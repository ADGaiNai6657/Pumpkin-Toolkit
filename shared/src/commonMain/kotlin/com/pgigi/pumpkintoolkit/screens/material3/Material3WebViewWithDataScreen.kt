package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.rememberSaveableWebViewStateWithData
import com.pgigi.pumpkintoolkit.LocalNavigator
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3WebViewWithDataScreen(html: String, defaultTitle: String = "加载中...") {
    val navigator = LocalNavigator.current
    val webViewState = rememberSaveableWebViewStateWithData(data = html)
    var title by remember { mutableStateOf(defaultTitle) }

    LaunchedEffect(webViewState.pageTitle) {
        title = webViewState.pageTitle ?: defaultTitle
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        ComposeWebView(
            state = webViewState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}
