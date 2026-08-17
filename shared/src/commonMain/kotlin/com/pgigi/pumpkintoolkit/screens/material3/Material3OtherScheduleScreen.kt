package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.components.material3.SchedulePager
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.utils.buildWeekCourses
import com.pgigi.pumpkintoolkit.viewmodel.OtherScheduleViewModel
import kotlinx.datetime.LocalDate
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ListView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3OtherScheduleScreen(
    viewModel: OtherScheduleViewModel = viewModel(factory = OtherScheduleViewModel.Factory)
) {
    val navigator = LocalNavigator.current
    var title by remember { mutableStateOf("课程表") }
    var loading by remember { mutableStateOf(true) }
    var initialPage by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    val list = remember { mutableStateListOf<Course>() }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var showTermPicker by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(viewModel.currentTermIndex) {
        loading = true
        if (viewModel.currentTermIndex < 0 || viewModel.currentTermIndex >= AppConfig.termValueList.size) {
            return@LaunchedEffect
        }
        val termId = AppConfig.termValueList[viewModel.currentTermIndex]
        if (viewModel.courseListMap[termId].isNullOrEmpty()) {
            val tmpList = QZClient.getAllCourses(termId)
            tmpList?.let {
                viewModel.courseListMap[termId] = tmpList
            }
        }
        viewModel.courseListMap[termId]?.let {
            list.clear()
            list.addAll(viewModel.courseListMap[termId]!!)
        }
        if (viewModel.startDateMap[termId] == null) {
            val tmpStartDate = QZClient.getStartDate(termId)
            tmpStartDate?.let {
                viewModel.startDateMap[termId] = tmpStartDate
            }
        }
        startDate = viewModel.startDateMap[termId]
        loading = false
    }

    val courseListByWeek by mutableStateOf(buildWeekCourses(list))
    pageCount = courseListByWeek.size
    initialPage = 0

    val pagerState = rememberPagerState(
        pageCount = { pageCount },
        initialPage = initialPage
    )

    LaunchedEffect(pagerState.pageCount) {
        pagerState.scrollToPage(0)
    }

    LaunchedEffect(pagerState.currentPage) {
        title = "第${pagerState.currentPage + 1}周"
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navigator.pop()
                    }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
                actions = {
                    if (AppConfig.termNameList.isNotEmpty()) {
                        IconButton(onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showTermPicker = true
                        }) {
                            Icon(MiuixIcons.ListView, contentDescription = "学期列表")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        SchedulePager(
            modifier = Modifier.padding(paddingValues),
            cellHeight = AppConfig.cellHeight.dp,
            courseListByWeek = courseListByWeek,
            pagerState = pagerState,
            timeList = AppConfig.timeList,
            startDate = startDate
        )
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
    }

    if (showTermPicker) {
        AlertDialog(
            onDismissRequest = { showTermPicker = false },
            title = { Text("选择学期") },
            text = {
                Column {
                    AppConfig.termNameList.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (index != viewModel.currentTermIndex) {
                                        viewModel.currentTermIndex = index
                                        title = "课程表"
                                        showTermPicker = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                color = if (index == viewModel.currentTermIndex)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            if (index == viewModel.currentTermIndex) {
                                Text(
                                    text = "\u2713",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTermPicker = false }) { Text("关闭") }
            }
        )
    }
}
