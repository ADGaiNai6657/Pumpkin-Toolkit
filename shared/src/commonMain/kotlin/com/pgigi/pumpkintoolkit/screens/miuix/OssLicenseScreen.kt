package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.models.OssLicense
import com.pgigi.pumpkintoolkit.utils.JsonUtil
import com.pgigi.pumpkintoolkit.utils.ResourceUtils
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OssLicenseScreen() {
    val navigator = LocalNavigator.current

    val libraries = remember {
        val json = ResourceUtils.readText("open-source-license.json")
        json?.let { JsonUtil.parseListJson(it, OssLicense.serializer()) } ?: emptyList()
    }

    val grouped = remember(libraries) {
        libraries.groupBy { it.group ?: "其他" }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "开放源代码许可",
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            grouped.forEach { (group, libs) ->
                item(key = "header_$group") {
                    SmallTitle(group)
                }
                item(key = "card_$group") {
                    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        libs.forEach { lib ->
                            BasicComponent(
                                title = lib.title,
                                endActions = {
                                    Text(
                                        text = lib.author,
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .weight(1f, fill = false),
                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                        textAlign = TextAlign.End,
                                    )
                                },
                                onClick = { navigator.push(Route.OssLicenseDetail(lib)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
