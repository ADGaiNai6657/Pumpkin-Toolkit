package com.pgigi.pumpkintoolkit.screens.miuix.sunshine

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.models.sunshine.GuestBookItem
import com.pgigi.pumpkintoolkit.utils.SunshineClient
import com.pgigi.pumpkintoolkit.utils.toLocalDateTime
import com.pgigi.pumpkintoolkit.viewmodel.sunshine.SunshineDetailViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.MoreCircle
import top.yukonga.miuix.kmp.icon.extended.Reply
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SunshineDetailScreen(item: GuestBookItem, viewModel: SunshineDetailViewModel = viewModel(factory = SunshineDetailViewModel.Factory)) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val client = SunshineClient

    LaunchedEffect(Unit) {
        if(viewModel.infoMap[item.id] == null) {
            client.getGuestBookInfo(item.id)?.let { response ->
                viewModel.infoMap[item.id] = response
            }
        }
    }

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
                        navigator.push(Route.WebView("http://usc.tabbycms.com/column/detail/index.shtml?id="+item.id))
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
        val cardPadding = PaddingValues(12.dp,6.dp)
        LazyColumn(modifier = Modifier
            .padding(paddingValues)
            .padding(horizontal = 16.dp)) {
            item {
                Text(text = htmlToAnnotatedString(item.title), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        cardPadding
                    )) {
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
                        Text(text = item.man)
                        Text(
                            text = item.addDate.toLocalDateTime().toString().replace("T", " ").replace("Z", ""),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Card(modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .border(
                            width = 1.dp,
                            color = when (item.currentStatus) {
                                "1" -> MiuixTheme.colorScheme.error
                                "9" -> MiuixTheme.colorScheme.primary
                                else -> MiuixTheme.colorScheme.onSurfaceContainer
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    ){
                        Text(text = when(item.currentStatus){
                            "1" -> "已转交"
                            "9" -> "已处理"
                            else -> "未知"
                            }, modifier = Modifier.padding(cardPadding),
                            color = when(item.currentStatus){
                                "1" -> MiuixTheme.colorScheme.error
                                "9" -> MiuixTheme.colorScheme.primary
                                else -> MiuixTheme.colorScheme.onSurfaceContainer
                            }
                        )
                    }
                }
                Text(modifier = Modifier.padding(cardPadding),text = htmlToAnnotatedString(item.content))

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
                            modifier = Modifier.padding(cardPadding)
                                .clickable(enabled = true,onClick = {
                                    uriHandler.openUri("http://usc.tabbycms.com/column/detail/index.shtml?id="+item.id)
                                }),
                            color = MiuixTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding)) {
                    Text(text = "受理单位: ${item.nextDealDepartmentName}", fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.align(Alignment.CenterVertically) )
                    Spacer(modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically))
                    Icon(imageVector = MiuixIcons.Show, contentDescription = "浏览次数", tint = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.CenterVertically))
                    Text(text = item.visitCount.toString(), fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.align(Alignment.CenterVertically) )
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                item.replyContent?.let{
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardPadding)
                        .padding(top = 16.dp)
                    ) {
                        Icon(imageVector = MiuixIcons.Reply, contentDescription = "头像",
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(8.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Column(modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)){
                            Text(text = item.nextDealDepartmentName)
                            Text(
                                text = item.replyTime?.toLocalDateTime().toString().replace("T", " ").replace("Z", ""),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                    Text(modifier = Modifier.padding(cardPadding),text = htmlToAnnotatedString(item.replyContent))
                    viewModel.infoMap[item.id]?.let { infoItem ->

                        infoItem.reply?.fileName?.let{
                            if(infoItem.reply.fileName.endsWith(suffix = ".jpg", ignoreCase = true) ||
                                infoItem.reply.fileName.endsWith(suffix = ".png", ignoreCase = true)||
                                infoItem.reply.fileName.endsWith(suffix = ".jpeg", ignoreCase = true)||
                                infoItem.reply.fileName.endsWith(suffix = ".gif", ignoreCase = true)||
                                infoItem.reply.fileName.endsWith(suffix = ".bmp", ignoreCase = true)||
                                infoItem.reply.fileName.endsWith(suffix = ".webp", ignoreCase = true)
                            ){
                                AsyncImage(
                                    model = "http://usc.tabbycms.com/"+infoItem.reply.file,
                                    contentDescription = "图片",
                                    modifier = Modifier
                                )
                            }else{
                                Text(
                                    text = "附件: ${infoItem.reply.fileName}",
                                    modifier = Modifier.padding(cardPadding)
                                        .clickable(enabled = true,onClick = {
                                            uriHandler.openUri("http://usc.tabbycms.com/column/detail/index.shtml?id="+item.id)
                                        }),
                                    color = MiuixTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
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