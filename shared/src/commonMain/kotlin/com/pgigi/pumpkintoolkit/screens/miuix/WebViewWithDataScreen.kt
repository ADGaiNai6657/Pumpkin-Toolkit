package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.rememberSaveableWebViewStateWithData
import com.pgigi.pumpkintoolkit.LocalNavigator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun WebViewWithDataScreen(html: String, defaultTitle: String = "加载中...") {
    val navigator = LocalNavigator.current
    val webViewState = rememberSaveableWebViewStateWithData(data = html)
    var title by remember { mutableStateOf(defaultTitle) }

    LaunchedEffect(webViewState.pageTitle) {
        title = webViewState.pageTitle?: defaultTitle
    }

    Scaffold(topBar = {
        SmallTopAppBar(
            title = title,
            navigationIcon = {
                IconButton(onClick = {
                    navigator.pop()
                }) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "返回"
                    )
                }
            }
        )
    }) {paddingValues ->
        ComposeWebView(
            state = webViewState,
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        )
    }
}