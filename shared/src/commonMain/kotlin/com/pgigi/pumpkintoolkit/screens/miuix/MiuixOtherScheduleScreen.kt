package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.components.SchedulePager
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.utils.JsonUtil
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.utils.buildWeekCourses
import com.pgigi.pumpkintoolkit.viewmodel.OtherScheduleViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Reset
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
fun MiuixOtherScheduleScreen(viewModel: OtherScheduleViewModel = viewModel(factory = OtherScheduleViewModel.Factory)){
    val navigator = LocalNavigator.current
    var title by remember { mutableStateOf("课程表") }
    val coroutineScope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var initialPage by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    val list = remember { mutableStateListOf<Course>() }
    var startDate by remember{ mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(viewModel.currentTermIndex) {
        loading = true
        if(viewModel.currentTermIndex < 0 || viewModel.currentTermIndex >= AppConfig.termValueList.size){
            return@LaunchedEffect
        }
        val termId = AppConfig.termValueList[viewModel.currentTermIndex]
        if(viewModel.courseListMap[termId].isNullOrEmpty()){
            val tmpList = QZClient.getAllCourses(termId)
            tmpList?.let {
                viewModel.courseListMap[termId] = tmpList
                println(JsonUtil.toListJson(tmpList, Course.serializer()))
            }
        }
        viewModel.courseListMap[termId]?.let {
            list.clear()
            list.addAll(viewModel.courseListMap[termId]!!)
        }
        if(viewModel.startDateMap[termId] == null){
            val tmpStartDate = QZClient.getStartDate(termId)
            tmpStartDate?.let {
                viewModel.startDateMap[termId] = tmpStartDate
            }
        }
        startDate = viewModel.startDateMap[termId]
        loading = false
    }

    val courseListByWeek by mutableStateOf(buildWeekCourses(list) )
    pageCount = courseListByWeek.size
    initialPage = 0

    val pagerState = rememberPagerState(
        pageCount = {pageCount},
        initialPage = initialPage
    )

    LaunchedEffect(pagerState.pageCount) {
        pagerState.scrollToPage(0)
    }

    LaunchedEffect(pagerState.currentPage) {
        title = "第${pagerState.currentPage + 1}周"
    }


    Scaffold(
        modifier = Modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmallTopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    val showPopup = remember { mutableStateOf(false) }

                    if(AppConfig.termNameList.isNotEmpty()){
                        IconButton(
                            onClick = {
                                showPopup.value = true
                            }
                        ) {
                            Icon(MiuixIcons.ListView, contentDescription = "学期列表")
                        }
                    }

                    WindowListPopup(
                        show = showPopup.value,
                        alignment = PopupPositionProvider.Align.Start,
                        onDismissRequest = { showPopup.value = false }
                    ) {
                        val dismiss = LocalDismissState.current
                        ListPopupColumn {
                            AppConfig.termNameList.forEachIndexed { index, string ->
                            DropdownImpl(
                                text = string,
                                optionSize = AppConfig.termNameList.size,
                                isSelected = viewModel.currentTermIndex == index,
                                onSelectedIndexChange = {
                                    if(index != viewModel.currentTermIndex){
                                        viewModel.currentTermIndex = index
                                        title = "课程表"
                                        dismiss?.invoke()
                                    }
                                },
                                index = index
                            )
                        }
                        }
                    }
                }
            )
        }
    ){paddingValues ->
        SchedulePager(modifier = Modifier.padding(paddingValues),
            cellHeight = AppConfig.cellHeight.dp,
            courseListByWeek = courseListByWeek,
            pagerState = pagerState,
            timeList = AppConfig.timeList,
            startDate = startDate
        )
        if(loading){
            InfiniteProgressIndicator(
                Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.windowDimming)
            )
        }

    }
}
