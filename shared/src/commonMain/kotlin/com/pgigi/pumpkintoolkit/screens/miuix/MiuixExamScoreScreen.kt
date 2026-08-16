package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.components.isLiquidGlassSupported
import com.pgigi.pumpkintoolkit.components.miuix.MiuixFloatingDropdown
import com.pgigi.pumpkintoolkit.constants.ScoreColor
import com.pgigi.pumpkintoolkit.models.ExamScore
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.viewmodel.ScoreViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixExamScoreScreen(viewModel: ScoreViewModel = viewModel(factory = ScoreViewModel.Factory)) {
    val navigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current
    var loading by remember { mutableStateOf(true) }
    val scoreList = remember { mutableStateListOf<ExamScore>() }

    val liquidGlassSupported = isLiquidGlassSupported()
    val useBlur = AppConfig.enableBlurEffect && liquidGlassSupported
    val backdrop = if (useBlur) rememberLayerBackdrop() else null

    LaunchedEffect(Unit) {
        viewModel.loadScoreCache()
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
        val cached = viewModel.scoreListMap[termId]
        if (cached != null) {
            scoreList.clear()
            scoreList.addAll(cached)
            loading = false
        } else {
            loading = true
        }
        val scores = QZClient.getExamScores(termId)
        if (scores != null) {
            viewModel.scoreListMap[termId] = scores
            scoreList.clear()
            scoreList.addAll(scores)
            viewModel.saveScoreCache()
        }
        loading = false
    }

    val shortLabels = remember(AppConfig.termNameList) {
        AppConfig.termNameList.map { it }
    }

    val currentLabel = shortLabels.getOrElse(viewModel.currentTermIndex) { "选择学期" }
    val containerColor = MiuixTheme.colorScheme.surfaceContainer

    val displayList = if (AppConfig.hideFailScore) scoreList.filter { it.grade >= 1f } else scoreList.toList()

    val totalCredit = displayList.sumOf { it.credit.toDouble() }
    val validGrades = displayList.filter { it.grade > 0f }
    val gpa = if (validGrades.isNotEmpty()) {
        validGrades.sumOf { (it.grade * it.credit).toDouble() } / validGrades.sumOf { it.credit.toDouble() }
    } else 0.0

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmallTopAppBar(
                title = "成绩查询",
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
                contentPadding = PaddingValues(12.dp, 6.dp, 12.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (displayList.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ScoreSummaryItem("课程数", "${displayList.size}")
                                ScoreSummaryItem("总学分", "${(totalCredit * 10).roundToInt() / 10.0}")
                                ScoreSummaryItem("平均绩点", "${(gpa * 100).roundToInt() / 100.0}")
                            }
                        }
                    }
                }
                items(displayList.size) { index ->
                    ScoreCard(score = displayList[index])
                }
                if (!loading && displayList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无成绩数据",
                                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (loading) {
                InfiniteProgressIndicator(
                    Modifier
                        .fillMaxSize()
                        .background(MiuixTheme.colorScheme.windowDimming)
                )
            }

            if (shortLabels.isNotEmpty()) {
                MiuixFloatingDropdown(
                    label = currentLabel,
                    items = shortLabels,
                    selectedIndex = viewModel.currentTermIndex,
                    onSelect = { index ->
                        if (index != viewModel.currentTermIndex) {
                            viewModel.currentTermIndex = index
                        }
                    },
                    backdrop = backdrop,
                    containerColor = containerColor
                )
            }
        }
    }
}

@Composable
private fun ScoreSummaryItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = MiuixTheme.textStyles.headline1.fontSize,
            color = MiuixTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariantActions
        )
    }
}

@Composable
private fun ScoreCard(score: ExamScore) {
    Card(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(
            title = score.name,
            summary = "学分: ${score.credit}   总学时: ${score.totalClassHours}",
            endActions = {
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    Text(
                        text = score.score,
                        fontSize = MiuixTheme.textStyles.headline1.fontSize,
                        color = when (score.grade) {
                            in 5f..10f -> ScoreColor.PERFECT
                            in 4f..<5f -> ScoreColor.EXCELLENT
                            in 3f..<4f -> ScoreColor.GOOD
                            in 2f..<3f -> ScoreColor.FAIR
                            in 1f..<2f -> ScoreColor.PASS
                            else -> ScoreColor.FAIL
                        },
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "绩点:${score.grade}",
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        textAlign = TextAlign.Center,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        )
    }
}
