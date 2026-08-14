package com.pgigi.pumpkintoolkit.screens.miuix.sunshine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.models.sunshine.LostItem
import com.pgigi.pumpkintoolkit.utils.toLocalDateTime
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.MoreCircle
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LostAndFoundDetailScreen(item: LostItem) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "详细",
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
                        navigator.push(Route.WebView("http://usc.tabbycms.com/column/swzlxq/index.shtml?id="+item.id))
                    }) {
                        Icon(
                            imageVector = MiuixIcons.MoreCircle,
                            contentDescription = "浏览器打开"
                        )
                    }
                }
            )
        }
    ) {paddingValues ->
        LazyColumn(modifier = Modifier
            .padding(paddingValues)
            .padding(horizontal = 16.dp)) {
            item {
                Row(modifier = Modifier
                    .fillMaxWidth()) {
                    Icon(imageVector = MiuixIcons.Contacts, contentDescription = "头像",
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Column(modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)){
                        Text(text = if(item.linkMan.isNullOrEmpty()) "匿名" else item.linkMan)
                        Text(
                            text = item.lostTime.toLocalDateTime().toString().replace("T", " ").replace("Z", ""),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                Text(
                    text = "物品信息",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Text(text = htmlToAnnotatedString(item.propertyName))

                Text(
                    text = "物品描述",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Text(text = htmlToAnnotatedString(item.description))

                Text(
                    text = "丢失地点",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Text(text = htmlToAnnotatedString(item.lostPlace))
                item.nowPlace?.let{
                    Text(
                        text = "招领地点",
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Text(text = htmlToAnnotatedString(item.nowPlace))
                }
                item.linkPhone?.let{
                    Text(
                        text = "联系电话",
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Text(text = htmlToAnnotatedString(item.linkPhone))
                }
                item.linkQQ?.let{
                    Text(
                        text = "联系QQ",
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Text(text = htmlToAnnotatedString(item.linkQQ))
                }

                item.attachName?.let{
                    if(item.attachName.endsWith(suffix = ".jpg", ignoreCase = true)||
                        item.attachName.endsWith(suffix = ".png", ignoreCase = true)||
                        item.attachName.endsWith(suffix = ".jpeg", ignoreCase = true)||
                        item.attachName.endsWith(suffix = ".gif", ignoreCase = true)||
                        item.attachName.endsWith(suffix = ".bmp", ignoreCase = true)||
                        item.attachName.endsWith(suffix = ".webp", ignoreCase = true)
                        ){
                        AsyncImage(
                            model = "http://usc.tabbycms.com/"+item.attach,
                            contentDescription = "图片",
                            modifier = Modifier
                        )
                    }else{
                        Text(
                            text = "附件: ${item.attachName}",
                            modifier = Modifier.padding(PaddingValues(12.dp,6.dp))
                                .clickable(enabled = true, onClick = {
                                    uriHandler.openUri("http://usc.tabbycms.com/column/swzlxq/index.shtml?id=" + item.id)
                                }),
                            color = MiuixTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                }
            }
        }
    }
}