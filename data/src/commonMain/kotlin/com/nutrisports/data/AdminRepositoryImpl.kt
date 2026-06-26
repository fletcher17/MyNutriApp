package com.nutrisports.data

import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
import com.nutrisports.data.domain.AdminRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withTimeout
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AdminRepositoryImpl : AdminRepository {

    override fun getCurrentUserId() = Firebase.auth.currentUser?.uid

    override suspend fun createNewProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val currentUserId = getCurrentUserId()

            if (currentUserId != null) {
                val firestore = Firebase.firestore
                val productCollection = firestore.collection("product")
                productCollection.document(product.id).set(product)
                onSuccess()
            } else {
                onError("User is not available")
            }
        } catch (e: Exception) {
            onError("Error while creating a new product: ${e.message}")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun uploadImageToStorage(file: File): String? {
        return if (getCurrentUserId() != null) {
            val storage = Firebase.storage.reference
            val imagePath = storage.child(path = "images/${Uuid.random().toHexString()}")
            try {
                withTimeout(timeMillis = 20000L) {
                    imagePath.putFile(file)
                    imagePath.getDownloadUrl()
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    override suspend fun deleteImageFromStorage(
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val storage = extractFirebaseStoragePath(downloadUrl)
            if (storage != null) {
                Firebase.storage.reference(storage).delete()
                onSuccess()
            } else {
                onError("Error while extracting storage path")
            }
        } catch (e: Exception) {
            onError("Error while deleting image from storage: ${e.message}")
        }

    }

    override fun readLastTenProduct(): Flow<RequestState<List<Product>>> = channelFlow {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                val database = Firebase.firestore
                database.collection(collectionPath = "product")
                    .orderBy("createdAt", Direction.DESCENDING)
                    .limit(10)
                    .snapshots
                    .collectLatest { query ->
                        val products = query.documents.map { document ->
                            Product(
                                id = document.id,
                                title = document.get(field = "title"),
                                description = document.get(field = "description"),
                                thumbnail = document.get(field = "thumbnail"),
                                category = document.get(field = "category"),
                                flavors = document.get(field = "flavors"),
                                weight = document.get(field = "weight"),
                                price = document.get(field = "price"),
                                isPopular = document.get(field = "isPopular"),
                                isDiscounted = document.get(field = "isDiscounted"),
                                isNew = document.get(field = "isNew")
                            )
                        }
                        send(RequestState.Success(data = products))
                    }
            } else {
                send(RequestState.Error("User is not available"))
            }

        } catch (e: Exception) {
            send(RequestState.Error("Error while reading the last 10 items from the database: ${e.message}"))
        }
    }

    override suspend fun readProductById(id: String): RequestState<Product> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val database = Firebase.firestore
                val document = database.collection(collectionPath = "product")
                    .document(id)
                    .get()
                if (document.exists) {
                    RequestState.Success(
                        Product(
                            id = document.id,
                            title = document.get(field = "title"),
                            description = document.get(field = "description"),
                            thumbnail = document.get(field = "thumbnail"),
                            category = document.get(field = "category"),
                            flavors = document.get(field = "flavors"),
                            weight = document.get(field = "weight"),
                            price = document.get(field = "price"),
                            isPopular = document.get(field = "isPopular"),
                            isDiscounted = document.get(field = "isDiscounted"),
                            isNew = document.get(field = "isNew")
                        )
                    )
                } else {
                    RequestState.Error("Selected Product not Found")
                }

            } else {
                RequestState.Error("User not Found")
            }
        } catch (e: Exception) {
            RequestState.Error("Error while reading product by id: ${e.message}")
        }
    }

    override suspend fun updateImageThumbnail(
        productId: String,
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val database = Firebase.firestore
                val productCollection = database.collection("product")
                val existingProduct = productCollection.document(productId)
                    .get()
                if (existingProduct.exists) {
                    productCollection.document(productId).updateFields {
                        "thumbnail" to downloadUrl
                    }
                    onSuccess()
                } else {
                    onError("Product not Found")
                }

            } else {
                onError("User not found")
            }

        } catch (e: Exception) {
            onError("Error while updating image thumbnail: ${e.message}")
        }
    }

    override suspend fun updateProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val database = Firebase.firestore
                val productReference = database.collection("product").document(product.id)
                val existingProduct = productReference
                    .get()
                if (existingProduct.exists) {
                    productReference.update(product)
                    onSuccess()
                } else {
                    onError("Product not Found")
                }
            } else {
                onError("User not found")
            }
        } catch (e: Exception) {
            onError("Error while updating product: ${e.message}")
        }
    }


    private fun extractFirebaseStoragePath(downloadUrl: String): String? {
        val startIndex = downloadUrl.indexOf("/o/") + 3
        if (startIndex < 3) return null

        val endIndex = downloadUrl.indexOf("?", startIndex)
        val encodePath = if (endIndex != -1) {
            downloadUrl.substring(startIndex, endIndex)
        } else {
            downloadUrl.substring(startIndex)
        }

        return decodeFirebasePath(encodePath)
    }

    private fun decodeFirebasePath(encodePath: String): String {
        return encodePath
            .replace("%2F", "/")
            .replace("%20", " ")
    }
}