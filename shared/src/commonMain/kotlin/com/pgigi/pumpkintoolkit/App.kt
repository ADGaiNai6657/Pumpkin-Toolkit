package com.pgigi.pumpkintoolkit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.ui.NavDisplay
import com.pgigi.pumpkintoolkit.constants.FileName
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.screens.miuix.MiuixHomeScreen
import com.pgigi.pumpkintoolkit.screens.miuix.MiuixLoginScreen
import com.pgigi.pumpkintoolkit.screens.miuix.MiuixSettingScreen
import com.pgigi.pumpkintoolkit.utils.FileStoreUtils
import com.pgigi.pumpkintoolkit.utils.JsonUtil
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.utils.WeekCalculator
import com.pgigi.pumpkintoolkit.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No Navigator found!") }

@Composable
fun App(
    viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
) {
    val navigator = remember(viewModel.backStack) { Navigator(viewModel.backStack) }
    val coroutineScope = rememberCoroutineScope()
    AppConfig.load()

    LaunchedEffect(AppConfig.startDate){
        AppConfig.startDate?.let{
            val weekCalculator = WeekCalculator(AppConfig.startDate!!,1)
            viewModel.currentWeek = weekCalculator.getWeekNumber(AppConfig.localDate).toInt()
        }
    }

    coroutineScope.launch {
        val courseStr = FileStoreUtils.readString(FileName.SCHEDULE)
        courseStr?.let {
            val list = JsonUtil.parseListJson(courseStr, Course.serializer())
            viewModel.courseList.clear()
            viewModel.courseList.addAll(list)
        }
    }

    QZClient.addLoginSuccessCallback {
        coroutineScope.launch {
            val list = QZClient.getAllCourses()
            list?.let {
                viewModel.courseList.clear()
                viewModel.courseList.addAll(list)
                FileStoreUtils.writeString(
                    FileName.SCHEDULE,
                    JsonUtil.toListJson(list, Course.serializer())
                )
            }
            val startDate = QZClient.getStartDate()
            startDate?.let {
                AppConfig.startDate = startDate
                AppConfig.save()
            }
            val totalWeek = QZClient.getWeekNum()
            totalWeek?.let {
                AppConfig.totalWeek = totalWeek
                AppConfig.save()
            }
        }
    }

    if(AppConfig.username.isNotEmpty() && AppConfig.password.isNotEmpty()){
        QZClient.username = AppConfig.username
        QZClient.password = AppConfig.password
        coroutineScope.launch {
            QZClient.login()
        }
    }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
    ){
        val controller = remember(AppConfig.colorSchemeMode) {
            when (AppConfig.colorSchemeMode) {
                ColorSchemeMode.Light -> ThemeController(ColorSchemeMode.Light)
                ColorSchemeMode.Dark -> ThemeController(ColorSchemeMode.Dark)
                else -> ThemeController(ColorSchemeMode.System)
            }
        }
        MiuixTheme(controller = controller){
            val entryProvider = remember(viewModel.backStack) {
                entryProvider<NavKey> {
                    entry<Route.Home> {
                        MiuixHomeScreen()
                    }
                    entry<Route.Login> {
                        MiuixLoginScreen()
                    }
                    entry<Route.Settings> {
                        MiuixSettingScreen()
                    }
                }
            }

            val entries = rememberDecoratedNavEntries(
                backStack = viewModel.backStack,
                entryProvider = entryProvider,
            )

            NavDisplay(
                entries = entries,
                onBack = { navigator.pop() }
            )
        }
    }
}