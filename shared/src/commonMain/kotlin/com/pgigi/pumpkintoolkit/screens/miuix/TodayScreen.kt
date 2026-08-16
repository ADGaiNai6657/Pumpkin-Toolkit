package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.pgigi.pumpkintoolkit.utils.buildWeekCourses
import com.pgigi.pumpkintoolkit.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Settings
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TodayScreen(modifier: Modifier = Modifier, viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val loggedIn = AppConfig.username.isNotEmpty() && AppConfig.password.isNotEmpty()
    val courses = buildWeekCourses(viewModel.courseList)
    val listState = rememberLazyListState()

    val navigator = LocalNavigator.current
    val windowInfo = LocalWindowInfo.current
    val screenWidthDp = windowInfo.containerDpSize.width

    val weekCourses = courses.getOrElse(viewModel.currentWeek-1) { emptyList() }
    val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val todayCourses = weekCourses.filter { course ->
        course.dayOfWeek == (localDate.dayOfWeek.ordinal + 1)%7
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmallTopAppBar(
                title = "今日课程",
                actions = {
                    AnimatedVisibility(screenWidthDp<=800.dp){
                        IconButton(onClick = { navigator.push(Route.Settings) }) {
                            Icon(MiuixIcons.Settings, contentDescription = "设置")
                        }
                    }
                },
            )
        }
    ) { paddingValues ->
        val cardPadding = PaddingValues(12.dp, 6.dp)
        LazyColumn(state = listState, modifier = Modifier.padding(paddingValues)) {
            items(todayCourses.size) { index ->
                val course = todayCourses[index]
                val classroom = course.classroom.replace("【红湘校区】", "")
                    .replace("【雨母校区】", "")

                val startIndex =
                    (course.lessonOfDay - 1).coerceIn(0, AppConfig.timeList.size - 1)
                val endIndex =
                    (startIndex + course.duration - 1).coerceIn(0, AppConfig.timeList.size - 1)

                Card(modifier = modifier.padding(cardPadding)) {
                    BasicComponent(
                        title = course.name,
                        summary = "${AppConfig.timeList[startIndex].start}-" +
                                "${AppConfig.timeList[endIndex].end} " +
                                course.teacher,
                        endActions = {
                            Text(
                                text = classroom,
                                textAlign = TextAlign.Center,
                            )
                        },
                    )
                }
            }
            if (!loggedIn || todayCourses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (loggedIn) "暂无课程" else "请登录使用",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .fillMaxSize(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}