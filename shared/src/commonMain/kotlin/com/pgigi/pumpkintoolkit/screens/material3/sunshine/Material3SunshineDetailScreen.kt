package com.pgigi.pumpkintoolkit.screens.material3.sunshine

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.models.sunshine.GuestBookItem
import com.pgigi.pumpkintoolkit.components.material3.M3Card
import com.pgigi.pumpkintoolkit.utils.SunshineClient
import com.pgigi.pumpkintoolkit.utils.toLocalDateTime
import com.pgigi.pumpkintoolkit.viewmodel.sunshine.SunshineDetailViewModel
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.MoreCircle
import top.yukonga.miuix.kmp.icon.extended.Reply
import top.yukonga.miuix.kmp.icon.extended.Show

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3SunshineDetailScreen(
    item: GuestBookItem,
    viewModel: SunshineDetailViewModel = viewModel(factory = SunshineDetailViewModel.Factory)
) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val client = SunshineClient
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        if (viewModel.infoMap[item.id] == null) {
            client.getGuestBookInfo(item.id)?.let { response ->
                viewModel.infoMap[item.id] = response
            }
        }
    }

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
                        navigator.push(Route.WebView("http://usc.tabbycms.com/column/detail/index.shtml?id=" + item.id))
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
                Text(
                    text = htmlToAnnotatedString(item.title),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = MiuixIcons.Contacts,
                        contentDescription = "头像",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(text = item.man)
                        Text(
                            text = item.addDate.toLocalDateTime().toString().replace("T", " ").replace("Z", ""),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    M3Card(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = when (item.currentStatus) {
                                "1" -> "已转交"
                                "9" -> "已处理"
                                else -> "未知"
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = when (item.currentStatus) {
                                "1" -> MaterialTheme.colorScheme.error
                                "9" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = htmlToAnnotatedString(item.content)
                )

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
                                    uriHandler.openUri("http://usc.tabbycms.com/column/detail/index.shtml?id=" + item.id)
                                },
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "受理单位: ${item.nextDealDepartmentName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f).align(Alignment.CenterVertically))
                    Icon(
                        imageVector = MiuixIcons.Show,
                        contentDescription = "浏览次数",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Text(
                        text = item.visitCount.toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                item.replyContent?.let { replyContent ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Reply,
                            contentDescription = "头像",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(8.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                            Text(text = item.nextDealDepartmentName)
                            Text(
                                text = item.replyTime?.toLocalDateTime().toString().replace("T", " ").replace("Z", ""),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = htmlToAnnotatedString(replyContent)
                    )
                    viewModel.infoMap[item.id]?.let { infoItem ->
                        infoItem.reply?.fileName?.let { fileName ->
                            if (fileName.endsWith(suffix = ".jpg", ignoreCase = true) ||
                                fileName.endsWith(suffix = ".png", ignoreCase = true) ||
                                fileName.endsWith(suffix = ".jpeg", ignoreCase = true) ||
                                fileName.endsWith(suffix = ".gif", ignoreCase = true) ||
                                fileName.endsWith(suffix = ".bmp", ignoreCase = true) ||
                                fileName.endsWith(suffix = ".webp", ignoreCase = true)
                            ) {
                                AsyncImage(
                                    model = "http://usc.tabbycms.com/" + infoItem.reply.file,
                                    contentDescription = "图片",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "附件: $fileName",
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            uriHandler.openUri("http://usc.tabbycms.com/column/detail/index.shtml?id=" + item.id)
                                        },
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
