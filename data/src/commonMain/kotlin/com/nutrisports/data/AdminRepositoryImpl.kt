package com.nutrisports.data

import com.nutrisport.shared.domain.Product
import com.nutrisports.data.domain.AdminRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

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
        } catch(e: Exception) {
            onError("Error while creating a new product: ${e.message}")
        }
    }
}