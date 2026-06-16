package com.nutrisports.data.domain

import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState

interface AdminRepository {

    fun getCurrentUserId(): String?

    suspend fun createNewProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}