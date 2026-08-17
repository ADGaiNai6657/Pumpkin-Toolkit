package com.pgigi.pumpkintoolkit.screens.material3.sunshine

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.components.material3.M3GroupHeader
import com.pgigi.pumpkintoolkit.components.material3.M3GroupSection
import com.pgigi.pumpkintoolkit.components.material3.M3Row
import com.pgigi.pumpkintoolkit.components.material3.M3SectionSpacer
import com.pgigi.pumpkintoolkit.utils.SunshineClient
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Phone
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Reply

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3SunshineMenuScreen() {
    val navigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("阳光平台") },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navigator.pop()
                    }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                M3GroupHeader("功能")
                M3GroupSection {
                    M3Row(
                        title = "列表清单",
                        icon = MiuixIcons.ListView,
                        onClick = { navigator.push(Route.SunshineList()) },
                    )
                    M3Row(
                        title = "政策咨询",
                        icon = MiuixIcons.Phone,
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.POLICY_CONSULTATION,
                                    submitUrl = "http://usc.tabbycms.com/column/wyzx/index.shtml"
                                )
                            )
                        },
                    )
                    M3Row(
                        title = "我要建议",
                        icon = MiuixIcons.Edit,
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.SUGGEST,
                                    submitUrl = "http://usc.tabbycms.com/column/wyjy/index.shtml"
                                )
                            )
                        },
                    )
                    M3Row(
                        title = "我要表扬",
                        icon = MiuixIcons.Favorites,
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.PRAISE,
                                    submitUrl = "http://usc.tabbycms.com/column/wyby/index.shtml"
                                )
                            )
                        },
                    )
                    M3Row(
                        title = "我要反映",
                        icon = MiuixIcons.Info,
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.REFLECT,
                                    submitUrl = "http://usc.tabbycms.com/column/wyfy/index.shtml"
                                )
                            )
                        },
                    )
                    M3Row(
                        title = "投诉受理",
                        icon = MiuixIcons.Reply,
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.COMPLAINT,
                                    submitUrl = "http://usc.tabbycms.com/column/wyts/index.shtml"
                                )
                            )
                        },
                    )
                    M3Row(
                        title = "失物招领",
                        icon = MiuixIcons.Promotions,
                        onClick = { navigator.push(Route.LostAndFoundList) },
                        showDivider = false
                    )
                }
                M3SectionSpacer()
            }
        }
    }
}
