package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.components.isLiquidGlassSupported
import com.pgigi.pumpkintoolkit.components.material3.M3Card
import com.pgigi.pumpkintoolkit.components.material3.M3FloatingDropdown
import com.pgigi.pumpkintoolkit.models.ExamInfo
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.viewmodel.ExamViewModel
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3ExamScreen(viewModel: ExamViewModel = viewModel(factory = ExamViewModel.Factory)) {
    val navigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current
    var loading by remember { mutableStateOf(true) }
    val examInfoList = remember { mutableStateListOf<ExamInfo>() }

    val liquidGlassSupported = isLiquidGlassSupported()
    val useBlur = AppConfig.enableBlurEffect && liquidGlassSupported
    val backdrop = if (useBlur) rememberLayerBackdrop() else null

    LaunchedEffect(Unit) {
        viewModel.loadExamCache()
        if (AppConfig.termValueList.isNotEmpty() && AppConfig.defaultTermId.isNotEmpty()) {
            val defaultIndex = AppConfig.termValueList.indexOf(AppConfig.defaultTermId)
            if (defaultIndex >= 0) {
                viewModel.currentTermIndex = defaultIndex
            }
        }
    }

    LaunchedEffect(viewModel.currentTermIndex) {
        val termId = if (viewModel.currentTermIndex in AppConfig.termValueList.indices) {
            AppConfig.termValueList[viewModel.currentTermIndex]
        } else {
            ""
        }
        val cached = viewModel.examInfoListMap[termId]
        if (cached != null) {
            examInfoList.clear()
            examInfoList.addAll(cached)
            loading = false
        } else {
            loading = true
        }
        val exams = QZClient.getExams(termId)
        if (exams != null) {
            viewModel.examInfoListMap[termId] = exams
            examInfoList.clear()
            examInfoList.addAll(exams)
            viewModel.saveExamCache()
        }
        loading = false
    }

    val shortLabels = remember(AppConfig.termNameList) {
//        AppConfig.termNameList.map { shortTermLabel(it) }
        AppConfig.termNameList.map { it }
    }

    val groupedExams = examInfoList
        .sortedBy { it.examTimeRaw }
        .groupBy { it.examWeek }
        .toList()
        .sortedBy { it.first }

    val currentLabel = shortLabels.getOrElse(viewModel.currentTermIndex) { "选择学期" }
    val containerColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("考试查询") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
                contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedExams.forEach { (week, exams) ->
                    item {
                        Text(
                            text = "第${week}周",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp)
                        )
                    }
                    items(exams.size) { index ->
                        M3ExamCard(examInfo = exams[index])
                    }
                }
                if (!loading && examInfoList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无考试安排",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (shortLabels.isNotEmpty()) {
                M3FloatingDropdown(
                    label = currentLabel,
                    items = shortLabels,
                    selectedIndex = viewModel.currentTermIndex,
                    onSelect = { index ->
                        if (index != viewModel.currentTermIndex) {
                            viewModel.currentTermIndex = index
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    backdrop = backdrop,
                    containerColor = containerColor
                )
            }
        }
    }
}

@Composable
private fun M3ExamCard(modifier: Modifier = Modifier, examInfo: ExamInfo) {
    val dayOfWeekStr = when (examInfo.examDayOfWeek) {
        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
        5 -> "周五"; 6 -> "周六"; 7 -> "周日"; else -> ""
    }
    M3Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = examInfo.courseName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$dayOfWeekStr  ${examInfo.examTimeRaw}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "地点: ${examInfo.examLocation}  ${examInfo.campus}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            examInfo.seatNumber?.let {
                Text(
                    text = "座位号: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
