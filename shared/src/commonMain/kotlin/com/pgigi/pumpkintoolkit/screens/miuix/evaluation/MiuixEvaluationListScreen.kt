package com.pgigi.pumpkintoolkit.screens.miuix.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.viewmodel.EvaluationViewModel
import kotlinx.coroutines.launch
import com.pgigi.pumpkintoolkit.models.evaluation.EvaluationListItem
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun MiuixEvaluationListScreen(
    actionUrl: String,
    title: String = "评教列表",
    viewModel: EvaluationViewModel = viewModel(factory = EvaluationViewModel.Factory)
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    val list = remember { mutableStateListOf<EvaluationListItem>() }
    var autoEvaluating by remember { mutableStateOf(false) }
    var autoProgress by remember { mutableStateOf("") }
    var autoResult by remember { mutableStateOf<String?>(null) }

    val refreshTrigger = navigator.observeResult<Boolean>("evaluation_refresh")
        .collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(actionUrl) {
        val cached = viewModel.listMap[actionUrl]
        if (cached != null) {
            list.clear()
            list.addAll(cached)
            loading = false
        } else {
            loading = true
            val items = QZClient.getEvaluationListItems(actionUrl)
            if (items != null) {
                viewModel.listMap[actionUrl] = items
                list.clear()
                list.addAll(items)
            }
            loading = false
        }
    }

    LaunchedEffect(refreshTrigger.value) {
        if (refreshTrigger.value) {
            viewModel.listMap.remove(actionUrl)
            loading = true
            val items = QZClient.getEvaluationListItems(actionUrl)
            if (items != null) {
                viewModel.listMap[actionUrl] = items
                list.clear()
                list.addAll(items)
            }
            loading = false
            navigator.clearResult("evaluation_refresh")
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        val cardPadding = PaddingValues(12.dp, 6.dp)
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(list.size) { index ->
                    val item = list[index]
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(cardPadding),
                        onClick = {
                            navigator.navigateForResult(
                                Route.EvaluationDetail(item.actionUrl, item.courseName),
                                "evaluation_refresh"
                            )
                        }
                    ) {
                        BasicComponent(
                            title = item.courseName,
                            summary = "${item.teacher} 评教分数: ${formatScore(item.score)}",
                            endActions = {
                                Text(
                                    text = if (item.isSubmit) "已评" else "未评",
                                    fontSize = MiuixTheme.textStyles.headline1.fontSize,
                                    color = if (item.isSubmit)
                                        MiuixTheme.colorScheme.primary
                                    else
                                        MiuixTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
                if (!loading && list.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无评教列表",
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }
                if (!loading && list.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PaddingValues(12.dp, 6.dp))
                        ) {
                            BasicComponent(
                                title = if (autoEvaluating) autoProgress else "一键评教",
                                summary = "自动选择最高分, 随机一项选第二高",
                                onClick = if (!autoEvaluating) {
                                    {
                                        coroutineScope.launch {
                                            autoEvaluating = true
                                            val targets = list.filter { !it.isSubmit }
                                            if (targets.isEmpty()) {
                                                autoResult = "没有需要评教的课程"
                                                autoEvaluating = false
                                                return@launch
                                            }
                                            var success = 0
                                            var fail = 0
                                            targets.forEachIndexed { index, item ->
                                                autoProgress = "正在评教 ${index + 1}/${targets.size}: ${item.courseName}"
                                                val msg = QZClient.autoEvaluate(item.actionUrl)
                                                if (msg != null && msg.contains("成功")) success++
                                                else fail++
                                            }
                                            autoEvaluating = false
                                            autoProgress = ""
                                            autoResult = "完成: 成功 $success, 失败 $fail"
                                            viewModel.listMap.remove(actionUrl)
                                            loading = true
                                            val items = QZClient.getEvaluationListItems(actionUrl)
                                            if (items != null) {
                                                viewModel.listMap[actionUrl] = items
                                                list.clear()
                                                list.addAll(items)
                                            }
                                            loading = false
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
            if (loading || autoEvaluating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    InfiniteProgressIndicator()
                }
            }
        }
    }
    autoResult?.let { msg ->
        WindowDialog(
            show = autoResult != null,
            title = "提示",
            summary = msg,
            onDismissRequest = { autoResult = null }
        ) {
            val dismiss = LocalDismissState.current
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                text = "确定",
                onClick = { dismiss?.invoke() }
            )
        }
    }
}

private fun formatScore(score: Float): String {
    return if (score % 1 == 0f) score.toInt().toString() else score.toString()
}
