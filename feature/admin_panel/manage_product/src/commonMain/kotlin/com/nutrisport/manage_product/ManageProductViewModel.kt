package com.nutrisport.manage_product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.RequestState
import com.nutrisports.data.domain.AdminRepository
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
data class ManageProductScreenState(
    val id: String = Uuid.random().toHexString(),
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val title: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val category: ProductCategory = ProductCategory.Protein,
    val flavors: String = "",
    val weight: Int? = null,
    val price: Double = 0.0,
)

class ManageProductViewModel(
    private val adminRepository: AdminRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val productId = savedStateHandle.get<String>("id") ?: ""
    var screenState by mutableStateOf(ManageProductScreenState())
        private set

    var thumbnailUploaderSate: RequestState<Unit> by mutableStateOf(RequestState.Idle)
        private set

    val isFormValid: Boolean
        get() = screenState.title.isNotEmpty() && screenState.thumbnail.isNotEmpty() &&
                screenState.description.isNotEmpty() && screenState.price > 0.0


    init {
        productId.takeIf { it.isNotEmpty() }?.let { id ->
            viewModelScope.launch {
                val selectedProduct = adminRepository.readProductById(id)
                if (selectedProduct.isSuccess()) {
                    val product = selectedProduct.getSuccessData()
                    updateProductId(product.id)
                    updateTitle(product.title)
                    updateCreatedAt(product.createdAt.toString())
                    updateDescription(product.description)
                    updateThumbnail(product.thumbnail)
                    updateThumbnailUploaderState(if (product.thumbnail.isNotEmpty()) RequestState.Success(Unit) else RequestState.Idle)
                    updateCategory(ProductCategory.fromString(product.category) ?: ProductCategory.Protein)
                    updateFlavors(product.flavors?.joinToString(",") ?: "")
                    updateWeight(product.weight)
                    updatePrice(product.price)
                }

            }
        }

    }

    fun updateProductId(value: String) {
        screenState = screenState.copy(
            id = value
        )
    }
    fun updateTitle(value: String) {
        screenState = screenState.copy(
            title = value
        )
    }

    fun updateCreatedAt(value: String) {
        screenState = screenState.copy(
            createdAt = value.toLong()
        )
    }

    fun updateDescription(value: String) {
        screenState = screenState.copy(
            description = value
        )
    }

    fun updateThumbnail(value: String) {
        screenState = screenState.copy(
            thumbnail = value
        )
    }

    fun updateThumbnailUploaderState(value: RequestState<Unit>) {
        thumbnailUploaderSate = value
    }

    fun updateCategory(value: ProductCategory) {
        screenState = screenState.copy(
            category = value
        )
    }

    fun updateFlavors(value: String) {
        screenState = screenState.copy(
            flavors = value
        )
    }

    fun updateWeight(value: Int?) {
        screenState = screenState.copy(
            weight = value
        )
    }

    fun updatePrice(value: Double) {
        screenState = screenState.copy(
            price = value
        )
    }

    fun createNewProduct(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            adminRepository.createNewProduct(
                product = Product(
                    id = screenState.id,
                    title = screenState.title,
                    description = screenState.description,
                    thumbnail = screenState.thumbnail,
                    category = screenState.category.title,
                    flavors = screenState.flavors.split(","),
                    weight = screenState.weight,
                    price = screenState.price
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    fun uploadThumbnailToStorage(file: File?, onSuccess: () -> Unit) {
        if (file == null) {
            updateThumbnailUploaderState(RequestState.Error("File is null, Error while selecting an image"))
            return
        }

        updateThumbnailUploaderState(RequestState.Loading)

        viewModelScope.launch {
            try {
                val downloadUrl = adminRepository.uploadImageToStorage(file)
                if (downloadUrl.isNullOrEmpty()) {
                    throw Exception("Failed to retrieve a download URL after the upload")
                }
                productId.takeIf { it.isNotEmpty() }?.let { id ->
                    adminRepository.updateImageThumbnail(
                        productId = id,
                        downloadUrl = downloadUrl,
                        onSuccess = {
                            onSuccess()
                            updateThumbnail(downloadUrl)
                            updateThumbnailUploaderState(RequestState.Success(Unit))
                        },
                        onError = { message ->
                            updateThumbnailUploaderState(RequestState.Error(message))
                        }
                    )
                } ?: run {
                    onSuccess()
                    updateThumbnail(downloadUrl)
                    updateThumbnailUploaderState(RequestState.Success(Unit))
                }
            } catch (e: Exception) {
                updateThumbnailUploaderState(RequestState.Error("Error while uploading: $e"))
            }
        }
    }


    fun updateProduct(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isFormValid) {
            viewModelScope.launch {
                adminRepository.updateProduct(
                    product = Product(
                        id = screenState.id,
                        title = screenState.title,
                        createdAt = screenState.createdAt,
                        description = screenState.description,
                        thumbnail = screenState.thumbnail,
                        category = screenState.category.name,
                        flavors = screenState.flavors.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() },
                        weight = screenState.weight,
                        price = screenState.price
                    ),
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        } else {
            onError("Please fill in the information")
        }

    }


    fun deleteImageFromStorage(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            adminRepository.deleteImageFromStorage(
                downloadUrl = screenState.thumbnail,
                onSuccess = {
                    productId.takeIf { it.isNotEmpty() }?.let { id ->
                        viewModelScope.launch {
                            adminRepository.updateImageThumbnail(
                                productId = id,
                                downloadUrl = "",
                                onSuccess = {
                                    updateThumbnail("")
                                    updateThumbnailUploaderState(RequestState.Idle)
                                    onSuccess()
                                },
                                onError = { message ->
                                    onError(message)
                                }
                            )
                        }
                    } ?: run {
                        updateThumbnail("")
                        updateThumbnailUploaderState(RequestState.Idle)
                        onSuccess()
                    }
                },
                onError = onError
            )
        }

    }

}