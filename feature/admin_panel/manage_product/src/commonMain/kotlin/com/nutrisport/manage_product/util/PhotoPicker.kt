package com.nutrisport.manage_product.util

import androidx.compose.runtime.Composable
import dev.gitlive.firebase.storage.File

expect class PhotoPicker {

    @Composable
    fun InitializePhotoPicker(onImageSelect: (File?) -> Unit)
    fun open()
}