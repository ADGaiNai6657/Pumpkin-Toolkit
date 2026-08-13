package com.pgigi.pumpkintoolkit

import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.viewmodel.AppViewModel

fun MainViewController() = ComposeUIViewController {
    App(
        viewModel = viewModel(factory = AppViewModel.Factory)
    )
}