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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalAppViewModel
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.components.miuix.SchedulePager
import com.pgigi.pumpkintoolkit.constants.FileName
import com.pgigi.pumpkintoolkit.constants.Texts
import com.pgigi.pumpkintoolkit.models.ScheduleCache
import com.pgigi.pumpkintoolkit.utils.FileStoreUtils
import com.pgigi.pumpkintoolkit.utils.JsonUtil
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.utils.buildWeekCourses
import com.pgigi.pumpkintoolkit.utils.reloadWidgetTimelines
import com.pgigi.pumpkintoolkit.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Reset
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun ScheduleScreen(modifier: Modifier = Modifier, viewModel: AppViewModel = LocalAppViewModel.current ){
    val loggedIn = AppConfig.username.isNotEmpty() && AppConfig.password.isNotEmpty()
    val navigator = LocalNavigator.current
    var title by remember { mutableStateOf("课程表") }
    val coroutineScope = rememberCoroutineScope()
    var initialPage by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    val windowInfo = LocalWindowInfo.current
    val screenWidthDp = windowInfo.containerDpSize.width
    val loading = loggedIn && viewModel.courseList.isEmpty()

    val courseListByWeek by mutableStateOf(buildWeekCourses(viewModel.courseList) )



    pageCount = maxOf(courseListByWeek.size,viewModel.currentWeek,AppConfig.totalWeek)
    initialPage = viewModel.currentWeek - 1


    val pagerState = rememberPagerState(
        pageCount = {pageCount},
        initialPage = initialPage.coerceIn(0, pageCount)
    )

    LaunchedEffect(pagerState.pageCount) {
        pagerState.scrollToPage((viewModel.currentWeek - 1).coerceIn(0,pageCount))
    }

    LaunchedEffect(pagerState.currentPage) {
        if(viewModel.courseList.isNotEmpty()){
            title = "第${pagerState.currentPage + 1}周"
        }
    }


    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmallTopAppBar(
                title = title,
                actions = {
                    AnimatedVisibility(screenWidthDp<=800.dp){
                        IconButton(onClick = { navigator.push(Route.Settings) }) {
                            Icon(MiuixIcons.Settings, contentDescription = "设置")
                        }
                    }
                },
                navigationIcon = {
                    AnimatedVisibility(
                        visible = maxOf(0, viewModel.currentWeek-1)!=pagerState.currentPage &&
                                AppConfig.startDate!=null &&
                                viewModel.courseList.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        viewModel.currentWeek-1
                                    )
                                }
                            }
                        ) {
                            Icon(MiuixIcons.Reset, contentDescription = "返回当前周")
                        }
                    }
                }
            )
        }
    ){paddingValues ->
        if (loggedIn) {
            var isRefreshing by rememberSaveable { mutableStateOf(false)}
            val pullToRefreshState = rememberPullToRefreshState()
            LaunchedEffect(isRefreshing) {
                if (isRefreshing) {
                    delay(60000.milliseconds)
                    isRefreshing = false
                }
            }
            PullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch {
                        val list = QZClient.getAllCourses()
                        list?.let {
                            viewModel.courseList.clear()
                            viewModel.courseList.addAll(list)
                            FileStoreUtils.writeString(
                                FileName.SCHEDULE,
                                JsonUtil.toJson(
                                    ScheduleCache(
                                        updateTime = Clock.System.now().toEpochMilliseconds(),
                                        courses = list
                                    ),
                                    ScheduleCache.serializer()
                                )
                            )
                        }
                        if(AppConfig.lockStartDate){
                            val startDate = QZClient.getStartDate()
                            startDate?.let {
                                AppConfig.startDate = startDate
                                AppConfig.save()
                            }
                        }
                        val totalWeek = QZClient.getWeekNum()
                        totalWeek?.let {
                            AppConfig.totalWeek = totalWeek
                            AppConfig.save()
                        }
                        val map = QZClient.getTermValueMap()
                        map?.let {
                            AppConfig.updateTermData(it)
                        }
                        isRefreshing = false
                        reloadWidgetTimelines()
                    }
                },
                pullToRefreshState = pullToRefreshState,
                modifier = Modifier.padding(paddingValues),
                refreshTexts = Texts.REFRESH_TEXTS,
            ) {
                SchedulePager(
                    modifier = Modifier.fillMaxSize(),
                    cellHeight = AppConfig.cellHeight.dp,
                    courseListByWeek = courseListByWeek,
                    pagerState = pagerState,
                    timeList = AppConfig.timeList,
                    startDate = AppConfig.startDate
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "请登录使用",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        if(loading){
            InfiniteProgressIndicator(
                Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.windowDimming)
            )
        }

    }

}


