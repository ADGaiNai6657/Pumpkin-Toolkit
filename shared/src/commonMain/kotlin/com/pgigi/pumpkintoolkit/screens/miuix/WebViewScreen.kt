package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.WebViewSettings
import com.parkwoocheol.composewebview.rememberWebViewController
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.viewmodel.WebViewViewModel
import io.ktor.http.URLParserException
import io.ktor.http.Url
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@Composable
fun WebViewScreen(url: String, defaultTitle: String = "加载中...", viewModel: WebViewViewModel = viewModel(factory = WebViewViewModel.Factory)){
    var title by remember { mutableStateOf(defaultTitle) }
    val state = viewModel.getWebViewState(url)
    val controller = rememberWebViewController()
    val navigator = LocalNavigator.current
    val show = remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(state.pageTitle) {
        title = state.pageTitle?: defaultTitle
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = title,
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        show.value = true
                    }) {
                        Icon(
                            imageVector = MiuixIcons.More,
                            contentDescription = "更多"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 上一页按钮，不可回退时置灰
                IconButton(
                    onClick = { controller.navigateBack() },
                    enabled = controller.canGoBack
                ) {
                    Icon(MiuixIcons.ChevronBackward, contentDescription = "上一页")
                }

                // 下一页按钮，不可前进时置灰
                IconButton(
                    onClick = { controller.navigateForward() },
                    enabled = controller.canGoForward
                ) {
                    Icon(MiuixIcons.ChevronForward, contentDescription = "下一页")
                }
            }
        }
    ) {paddingValues ->
        ComposeWebView(
            state = state,
            controller = controller,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            settings = WebViewSettings(
                javaScriptEnabled = true,
                domStorageEnabled = true
            ),
        )
        WindowBottomSheet(
            show = show.value,
            onDismissRequest = { show.value = false },
        ) {
            BasicComponent(title = "本页面由 ${parseHost(url)} 提供")
            ArrowPreference(
                title="默认浏览器打开",
                onClick = {
                    uriHandler.openUri(url)
                }
            )
            BasicComponent()
        }
    }
}

fun parseHost(urlString: String): String? {
    return try {
        val url = Url(urlString)
        url.host // 返回: ai.usc.edu.cn
    } catch (e: URLParserException) {
        null
    }
}