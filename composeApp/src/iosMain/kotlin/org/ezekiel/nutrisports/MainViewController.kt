package org.ezekiel.nutrisports

import androidx.compose.ui.window.ComposeUIViewController
import com.nutrisports.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }