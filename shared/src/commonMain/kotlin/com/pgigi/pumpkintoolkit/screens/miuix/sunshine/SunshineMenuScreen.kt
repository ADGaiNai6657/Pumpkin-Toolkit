package com.pgigi.pumpkintoolkit.screens.miuix.sunshine

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.utils.SunshineClient
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Phone
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Reply
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
fun SunshineMenuScreen() {
    val navigator = LocalNavigator.current

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "阳光平台",
                navigationIcon = {
                     IconButton(onClick = {
                         navigator.pop()
                     }) {
                         Icon(MiuixIcons.Back, contentDescription = "返回")
                     }
                },
            )
        }
    ) {paddingValues ->
        val cardPadding = PaddingValues(12.dp,6.dp)
        LazyColumn(modifier = Modifier.padding(paddingValues)){
            item {
                Card(modifier = Modifier.padding(cardPadding)) {
                    ArrowPreference(
                        startAction = {
                            Icon(MiuixIcons.ListView, contentDescription = "列表清单")
                        },
                        title = "列表清单",
                        onClick = {
                            navigator.push(Route.SunshineList())
                        }
                    )
                }
                Card(modifier = Modifier.padding(cardPadding)) {
                    ArrowPreference(
                        startAction = {
                            Icon(MiuixIcons.Phone, contentDescription = "政策咨询")
                        },
                        title = "政策咨询",
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.POLICY_CONSULTATION,
                                    submitUrl = "http://usc.tabbycms.com/column/wyzx/index.shtml"
                                )
                            )
                        }
                    )
                }
                Card(modifier = Modifier.padding(cardPadding)) {
                    ArrowPreference(
                        startAction = {
                            Icon(MiuixIcons.Edit, contentDescription = "我要建议")
                        },
                        title = "我要建议",
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.SUGGEST,
                                    submitUrl = "http://usc.tabbycms.com/column/wyjy/index.shtml"
                                )
                            )
                        }
                    )
                }
                Card(modifier = Modifier.padding(cardPadding)) {
                    ArrowPreference(
                        startAction = {
                            Icon(MiuixIcons.Favorites, contentDescription = "我要表扬")
                        },
                        title = "我要表扬",
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.PRAISE,
                                    submitUrl = "http://usc.tabbycms.com/column/wyby/index.shtml"
                                )
                            )
                        }
                    )
                }
                Card(modifier = Modifier.padding(cardPadding)) {
                    ArrowPreference(
                        startAction = {
                            Icon(MiuixIcons.Info, contentDescription = "我要反映")
                        },
                        title = "我要反映",
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.REFLECT,
                                    submitUrl = "http://usc.tabbycms.com/column/wyfy/index.shtml"
                                )
                            )
                        }
                    )
                }
                Card(modifier = Modifier.padding(cardPadding)) {
                    ArrowPreference(
                        startAction = {
                            Icon(MiuixIcons.Reply, contentDescription = "投诉受理")
                        },
                        title = "投诉受理",
                        onClick = {
                            navigator.push(
                                Route.SunshineList(
                                    typeCode = SunshineClient.TypeCode.COMPLAINT,
                                    submitUrl = "http://usc.tabbycms.com/column/wyts/index.shtml"
                                )
                            )
                        }
                    )
                }
                Card(modifier = Modifier.padding(cardPadding)) {
                    ArrowPreference(
                        startAction = {
                            Icon(MiuixIcons.Promotions, contentDescription = "失物招领")
                        },
                        title = "失物招领",
                        onClick = {
                            navigator.push(Route.LostAndFoundList)
                        }
                    )
                }
            }
        }
    }
}