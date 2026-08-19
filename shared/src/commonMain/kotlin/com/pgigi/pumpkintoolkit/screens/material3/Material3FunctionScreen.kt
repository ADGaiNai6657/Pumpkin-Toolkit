package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.components.material3.M3GroupSection
import com.pgigi.pumpkintoolkit.components.material3.M3Row
import com.pgigi.pumpkintoolkit.utils.QZClient
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Backup
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Location
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3FunctionScreen(modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.current

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(title = { Text("功能") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // M3GroupHeader("账号")
            M3GroupSection {
                M3Row(
                    title = "教务系统账号",
                    summary = AppConfig.username.ifEmpty { "未登录" },
                    icon = MiuixIcons.Contacts,
                    onClick = { navigator.push(Route.Login) },
                    showDivider = false
                )
            }

            // M3GroupHeader("设置")
            M3GroupSection {
                M3Row(
                    title = "设置",
                    icon = MiuixIcons.Settings,
                    onClick = { navigator.push(Route.Settings) },
                    showDivider = false
                )
            }

            // M3GroupHeader("查询")
            M3GroupSection {
                M3Row(
                    title = "考试查询",
                    icon = MiuixIcons.SelectAll,
                    onClick = { navigator.push(Route.Exam) },
                    showDivider = false
                )
                M3Row(
                    title = "成绩查询",
                    icon = MiuixIcons.File,
                    onClick = { navigator.push(Route.ExamScore) },
                    showDivider = false
                )
                M3Row(
                    title = "空教室查询",
                    icon = MiuixIcons.Location,
                    onClick = { navigator.push(Route.EmptyRoom) },
                    showDivider = false
                )
                M3Row(
                    title = "课程执行计划",
                    icon = MiuixIcons.Notes,
                    onClick = { navigator.push(Route.Plan) },
                    showDivider = false
                )
                M3Row(
                    title = "查看其他学期课表",
                    icon = MiuixIcons.VerticalSplit,
                    onClick = { navigator.push(Route.OtherSchedule) },
                    showDivider = false
                )
                M3Row(
                    title = "学生评教",
                    icon = MiuixIcons.Edit,
                    onClick = { navigator.push(Route.EvaluationMenu) },
                    showDivider = false
                )
                M3Row(
                    title = "第二课堂成绩单",
                    icon = MiuixIcons.Background,
                    onClick = {navigator.push(Route.WebView("https://m1wxluid.yichafen.com/","第二课堂成绩单"))}
                )
            }

            // M3GroupHeader("教务系统")
            M3GroupSection {
                M3Row(
                    title = "教务系统",
                    icon = MiuixIcons.Backup,
                    onClick = {
                        navigator.push(
                            Route.WebView(
                                QZClient.loginRedirectUrl.ifEmpty { AppConfig.serverUrl },
                                "教务系统"
                            )
                        )
                    },
                    showDivider = false
                )
                M3Row(
                    title = "线上注册及成绩单",
                    summary = "与教务系统是两个系统, 可看专业排名成绩单",
                    icon = MiuixIcons.File,
                    onClick = {
                        navigator.push(Route.WebView("https://ai.usc.edu.cn:9080/gztcyAPP/", "排名成绩单"))
                    },
                    showDivider = false
                )
            }

            // M3GroupHeader("其他")
            M3GroupSection {
                M3Row(
                    title = "阳光平台",
                    icon = MiuixIcons.Send,
                    onClick = { navigator.push(Route.SunshineMenu) },
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
//            M3SectionSpacer()
//            M3SectionSpacer()
        }
    }
}
