package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.utils.QZClient
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextButtonColors
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Backup
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Location
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun FunctionScreen(modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.current
    rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember{ SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var loading by remember{ mutableStateOf(false)}

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmallTopAppBar(title = "功能")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val cardPadding = PaddingValues(12.dp, 6.dp)
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(state = rememberScrollState())) {
            Card(modifier = modifier.padding(cardPadding)) {
                ArrowPreference(
                    title = "教务系统账号",
                    summary = AppConfig.username.ifBlank { "未登录" },
                    onClick = {
                        navigator.push(Route.Login)
                    },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Contacts,
                            contentDescription = "账号"
                        )
                    }
                )
                AnimatedVisibility(
                    AppConfig.username.isNotBlank() &&
                            AppConfig.password.isNotBlank()
                ){
                    ArrowPreference(
                        title = if (loading) "正在刷新登录状态..." else "点击刷新登录状态",
                        enabled = !loading,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Refresh,
                                contentDescription = "刷新"
                            )
                        },
                        endActions = {
                            if(loading){
                                InfiniteProgressIndicator()
                            }
                        },
                        onClick = {
                            loading = true
                            coroutineScope.launch {
                                QZClient.login(
                                    onSuccess = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "登录成功",
                                                withDismissAction = true
                                            )
                                        }
                                        loading = false
                                    },
                                    onFailure = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = it,
                                                withDismissAction = true
                                            )
                                        }
                                        loading = false
                                    }
                                )
                            }
                        }
                    )
                }
            }
            Card(modifier = modifier.padding(cardPadding)) {
                ArrowPreference(title = "设置", onClick = {
                    navigator.push(Route.Settings)
                },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Settings,
                            contentDescription = "设置"
                        )
                    }
                )
            }
            Card(modifier = modifier.padding(cardPadding)) {
                ArrowPreference(title = "考试查询", onClick = {
                    navigator.push(Route.Exam)
                },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.SelectAll,
                            contentDescription = "考试查询"
                        )
                    })
                ArrowPreference(title = "成绩查询", onClick = {
                    navigator.push(Route.ExamScore)
                },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.File,
                            contentDescription = "成绩查询"
                        )
                    })
                ArrowPreference(title = "空教室查询", onClick = {
                    navigator.push(Route.EmptyRoom)
                },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Location,
                            contentDescription = "空教室查询"
                        )
                    })
                ArrowPreference(title = "课程执行计划", onClick = {
                    navigator.push(Route.Plan)
                },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Notes,
                            contentDescription = "课程执行计划"
                        )
                    })
                ArrowPreference(title = "查看其他学期课表", onClick = {
                    navigator.push(Route.OtherSchedule)
                },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.VerticalSplit,
                            contentDescription = "其他学期课表"
                        )
                    })
                ArrowPreference(title = "学生评教", onClick = {
                        navigator.push(Route.EvaluationMenu)
                    },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Edit,
                            contentDescription = "学生评教"
                        )
                    }
                )
                ArrowPreference(title = "第二课堂成绩单", onClick = {
                        navigator.push(Route.WebView("https://m1wxluid.yichafen.com/","第二课堂成绩单"))
                    }, startAction = {
                        Icon(
                            imageVector = MiuixIcons.Background,
                            contentDescription = "第二课堂成绩单"
                        )
                    }
                )
            }
            Card(modifier = modifier.padding(cardPadding)) {
                ArrowPreference(title = "教务系统",
                    onClick = {
                        navigator.push(Route.WebView(
                            QZClient.loginRedirectUrl.ifEmpty { AppConfig.serverUrl }
                            ,"教务系统"))
                    },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Backup,
                            contentDescription = "教务系统"
                        )
                    }
                )
                ArrowPreference(title = "线上注册及成绩单", summary = "与教务系统是两个系统, 可看专业排名成绩单",
                    onClick = {
                        navigator.push(Route.WebView("https://ai.usc.edu.cn:9080/gztcyAPP/","排名成绩单"))
                    },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.File,
                            contentDescription = "线上注册及成绩单"
                        )
                    }
                )
            }
            Card(modifier = modifier.padding(cardPadding)) {
                ArrowPreference(title = "阳光平台",
                    onClick = {
                        navigator.push(Route.SunshineMenu)
                    },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Send,
                            contentDescription = "阳光平台"
                        )
                    }
                )
            }
            BasicComponent()
        }
    }
    WindowDialog(
        show = showLogoutDialog,
        title = "提示",
        summary = "是否退出登录?",
        onDismissRequest = { showLogoutDialog = false }
    ){
        val dismiss = LocalDismissState.current

        Row {
            TextButton(
                text = "取消",
                onClick = { dismiss?.invoke() },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
            TextButton(
                text = "确认",
                colors = TextButtonColors(
                    MiuixTheme.colorScheme.primary,
                    MiuixTheme.colorScheme.disabledPrimary,
                    MiuixTheme.colorScheme.onPrimary,
                    MiuixTheme.colorScheme.disabledOnPrimary
                ),
                onClick = {
                    AppConfig.username = ""
                    AppConfig.password = ""
//                    AppConfig.saveAccount()
//                    client.logged = false
//                    client.cookies = emptyList()
                    dismiss?.invoke()
                    navigator.replace(Route.Login)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
        }
    }
}