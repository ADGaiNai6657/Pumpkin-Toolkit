package com.pgigi.pumpkintoolkit.screens.material3.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.components.material3.M3Card
import com.pgigi.pumpkintoolkit.components.material3.M3GroupSection
import com.pgigi.pumpkintoolkit.components.material3.M3Row
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.viewmodel.EvaluationViewModel
import kotlinx.coroutines.launch
import test.xspj.EvaluationListItem
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3EvaluationListScreen(
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(list.size) { index ->
                    val item = list[index]
                    M3GroupSection {
                        M3Row(
                            title = item.courseName,
                            summary = "${item.teacher}  评价分数: ${formatScore(item.score)}",
                            onClick = {
                                navigator.navigateForResult(
                                    Route.EvaluationDetail(item.actionUrl, item.courseName),
                                    "evaluation_refresh"
                                )
                            },
                            showDivider = false,
                            trailingContent = {
                                Text(
                                    text = if (item.isSubmit) "已评" else "未评",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.isSubmit)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
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
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (!loading && list.isNotEmpty()) {
                    item {
                        M3Card(
                            modifier = Modifier.fillMaxWidth(),
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
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (autoEvaluating) autoProgress else "一键评教",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "自动选择最高分, 随机一项选第二高",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            if (loading || autoEvaluating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    autoResult?.let { msg ->
        AlertDialog(
            onDismissRequest = { autoResult = null },
            title = { Text("提示") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { autoResult = null }) {
                    Text("确定")
                }
            }
        )
    }
}

private fun formatScore(score: Float): String {
    return if (score % 1 == 0f) score.toInt().toString() else score.toString()
}
