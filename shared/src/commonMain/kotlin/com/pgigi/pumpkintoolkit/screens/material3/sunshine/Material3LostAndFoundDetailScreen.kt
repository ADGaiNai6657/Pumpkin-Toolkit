package com.pgigi.pumpkintoolkit.screens.material3.sunshine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.models.sunshine.LostItem
import com.pgigi.pumpkintoolkit.utils.toLocalDateTime
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.MoreCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3LostAndFoundDetailScreen(item: LostItem) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val hapticFeedback = LocalHapticFeedback.current

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("详细") },
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
                        navigator.push(Route.WebView("http://usc.tabbycms.com/column/swzlxq/index.shtml?id=" + item.id))
                    }) {
                        Icon(MiuixIcons.MoreCircle, contentDescription = "浏览器打开")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier
            .padding(paddingValues)
            .padding(horizontal = 16.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = MiuixIcons.Contacts,
                        contentDescription = "头像",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(text = if (item.linkMan.isNullOrEmpty()) "匿名" else item.linkMan)
                        Text(
                            text = item.lostTime.toLocalDateTime().toString().replace("T", " ").replace("Z", ""),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "物品信息",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = htmlToAnnotatedString(item.propertyName))

                Text(
                    text = "物品描述",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = htmlToAnnotatedString(item.description))

                Text(
                    text = "丢失地点",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = htmlToAnnotatedString(item.lostPlace))

                item.nowPlace?.let {
                    Text(
                        text = "招领地点",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = htmlToAnnotatedString(it))
                }
                item.linkPhone?.let {
                    Text(
                        text = "联系电话",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = htmlToAnnotatedString(it))
                }
                item.linkQQ?.let {
                    Text(
                        text = "联系QQ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = htmlToAnnotatedString(it))
                }

                item.attachName?.let { attachName ->
                    if (attachName.endsWith(suffix = ".jpg", ignoreCase = true) ||
                        attachName.endsWith(suffix = ".png", ignoreCase = true) ||
                        attachName.endsWith(suffix = ".jpeg", ignoreCase = true) ||
                        attachName.endsWith(suffix = ".gif", ignoreCase = true) ||
                        attachName.endsWith(suffix = ".bmp", ignoreCase = true) ||
                        attachName.endsWith(suffix = ".webp", ignoreCase = true)
                    ) {
                        AsyncImage(
                            model = "http://usc.tabbycms.com/" + item.attach,
                            contentDescription = "图片",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "附件: $attachName",
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable {
                                    uriHandler.openUri("http://usc.tabbycms.com/column/swzlxq/index.shtml?id=" + item.id)
                                },
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }
    }
}
