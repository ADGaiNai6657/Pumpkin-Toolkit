package com.pgigi.pumpkintoolkit.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.parkwoocheol.composewebview.WebViewState
import com.parkwoocheol.composewebview.rememberSaveableWebViewState

class WebViewViewModel : ViewModel() {

    // 使用 Map 缓存不同 URL 的 WebViewState
    private val webViewStates = mutableMapOf<String, WebViewState>()

    @Composable
    fun getWebViewState(url: String): WebViewState {
        return webViewStates.getOrPut(url) {
            // 创建但不触发重组
            rememberSaveableWebViewState(url = url)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WebViewViewModel()
            }
        }
    }
}