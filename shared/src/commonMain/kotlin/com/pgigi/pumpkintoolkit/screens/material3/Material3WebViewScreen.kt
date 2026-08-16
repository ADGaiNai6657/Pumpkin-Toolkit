package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.WebViewSettings
import com.parkwoocheol.composewebview.rememberWebViewController
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.components.material3.M3Row
import com.pgigi.pumpkintoolkit.viewmodel.WebViewViewModel
import io.ktor.http.URLParserException
import io.ktor.http.Url
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.More

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3WebViewScreen(
    url: String,
    defaultTitle: String = "加载中...",
    viewModel: WebViewViewModel = viewModel(factory = WebViewViewModel.Factory)
) {
    var title by remember { mutableStateOf(defaultTitle) }
    val state = viewModel.getWebViewState(url)
    val controller = rememberWebViewController()
    val navigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(state.pageTitle) {
        title = state.pageTitle ?: defaultTitle
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navigator.pop()
                    }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showBottomSheet = true
                    }) {
                        Icon(MiuixIcons.More, contentDescription = "更多")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        controller.navigateBack()
                    },
                    enabled = controller.canGoBack
                ) {
                    Icon(MiuixIcons.ChevronBackward, contentDescription = "上一页")
                }
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        controller.navigateForward()
                    },
                    enabled = controller.canGoForward
                ) {
                    Icon(MiuixIcons.ChevronForward, contentDescription = "下一页")
                }
            }
        }
    ) { paddingValues ->
        ComposeWebView(
            state = state,
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            settings = WebViewSettings(
                javaScriptEnabled = true,
                domStorageEnabled = true
            )
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                M3Row(
                    title = "本页面由 ${parseHost(url)} 提供",
                    showDivider = true,
                    onClick = { }
                )
                M3Row(
                    title = "默认浏览器打开",
                    showDivider = false,
                    onClick = {
                        uriHandler.openUri(url)
                        showBottomSheet = false
                    }
                )
            }
        }
    }
}

fun parseHost(urlString: String): String? {
    return try {
        val url = Url(urlString)
        url.host
    } catch (e: URLParserException) {
        null
    }
}
