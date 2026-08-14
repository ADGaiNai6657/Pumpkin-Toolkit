package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.components.NumberDatePicker
import com.pgigi.pumpkintoolkit.components.rememberNumberDatePickerState
import com.pgigi.pumpkintoolkit.constants.TimeList
import com.pgigi.pumpkintoolkit.getPlatform
import com.pgigi.pumpkintoolkit.utils.ResourceUtils
import com.pgigi.pumpkintoolkit.utils.WeekCalculator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextButtonColors
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.RangeSliderPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.math.roundToInt
import kotlin.time.Clock

@Composable
fun MiuixSettingScreen() {
    val navigator = LocalNavigator.current
    val showDialog = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "设置",
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val cardPadding = PaddingValues(12.dp, 6.dp)
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SmallTitle("主题设置")
            Card(modifier = Modifier.padding(cardPadding)){
                val colorItems = listOf("跟随系统", "浅色模式", "深色模式")
                var selectedColor by remember { mutableIntStateOf(0) }
                selectedColor = when (AppConfig.colorSchemeMode) {
                    ColorSchemeMode.Light -> 1
                    ColorSchemeMode.Dark -> 2
                    else -> 0
                }
                WindowDropdownPreference(
                    title = "颜色模式",
                    items = colorItems,
                    selectedIndex = selectedColor,
                    onSelectedIndexChange = {
                        if (it != selectedColor) {
                            selectedColor = it
                            AppConfig.colorSchemeMode = when (selectedColor) {
                                1 -> ColorSchemeMode.Light
                                2 -> ColorSchemeMode.Dark
                                else -> ColorSchemeMode.System
                            }
                            AppConfig.save()
                        }
                    }
                )
                /*SwitchPreference(
                    title = "悬浮导航栏",
                    checked = AppConfig.floatingNavigation,
                    onCheckedChange = {
                        AppConfig.floatingNavigation = it
                        AppConfig.save()
                    }
                )*/
            }

            SmallTitle("课表设置")
            Card(modifier = Modifier.padding(cardPadding)) {
                /*val cellTypeItems = listOf("彩底白字", "彩底彩字")
                var selectedCellType by remember { mutableIntStateOf(0) }
                selectedCellType = AppConfig.cellType
                WindowDropdownPreference(
                    title = "课程表单元格样式",
                    items = cellTypeItems,
                    selectedIndex = selectedCellType,
                    onSelectedIndexChange = {
                        if(it != selectedCellType){
                            selectedCellType = it
                            AppConfig.cellType = selectedCellType
                            AppConfig.saveSchedule()
                        }
                    }
                )*/
                val timeTypeItems = listOf("自动切换", "夏秋时间", "秋冬时间")
                var selectedTimeType by remember { mutableIntStateOf(0) }
                selectedTimeType = AppConfig.timeSeason
                WindowDropdownPreference(
                    title = "课程时间",
                    items = timeTypeItems,
                    selectedIndex = selectedTimeType,
                    onSelectedIndexChange = {
                        if(it != selectedTimeType){
                            selectedTimeType = it
                            AppConfig.timeSeason = selectedTimeType
                            AppConfig.save()
                            val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                            AppConfig.timeList = when(AppConfig.timeSeason){
                                1 -> TimeList.summerAutumnTime
                                2 -> TimeList.winterSpringTime
                                else -> if(localDate.month.number in 5..<10) TimeList.summerAutumnTime else TimeList.winterSpringTime
                            }
                        }
                    }
                )
                ArrowPreference(title = "课表开始时间",
                    endActions = {
                        Text(if(AppConfig.startDate==null) "" else AppConfig.startDate.toString())
                    },
                    onClick = {
                        if(AppConfig.startDate == null){
                            datePickerState.selectedDateMillis = Clock.System.now().toEpochMilliseconds()
                        }else{
                            datePickerState.selectedDateMillis = AppConfig.startDate!!.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                        }
                        showDialog.value = true
                    }
                )

                var cellHeight by remember{ mutableStateOf(AppConfig.cellHeight.toFloat()) }
                SliderPreference(
                    title = "单元格高度",
                    value = cellHeight,
                    onValueChange = {
                        cellHeight = it
                        AppConfig.cellHeight = it.roundToInt()
                        AppConfig.save()
                    },
                    valueRange = 50f..100f,
                    steps = 9,
                    endActions = {
                        Text("$cellHeight dp")
                    }
                )
                val lineItems = listOf("1","2","3","4")
                WindowDropdownPreference(
                    title = "课程名称显示行数",
                    items = lineItems,
                    selectedIndex = AppConfig.courseNameLine - 1,
                    onSelectedIndexChange = {
                        AppConfig.courseNameLine = it + 1
                        AppConfig.save()
                    }
                )
                WindowDropdownPreference(
                    title = "课程教室显示行数",
                    items = lineItems,
                    selectedIndex = AppConfig.courseRoomLine - 1,
                    onSelectedIndexChange = {
                        AppConfig.courseRoomLine = it + 1
                        AppConfig.save()
                    }
                )
                WindowDropdownPreference(
                    title = "课程教师显示行数",
                    items = lineItems,
                    selectedIndex = AppConfig.courseTeacherLine - 1,
                    onSelectedIndexChange = {
                        AppConfig.courseTeacherLine = it + 1
                        AppConfig.save()
                    }
                )
            }

            /*SmallTitle("成绩查询")
            Card(modifier = Modifier.padding(cardPadding)) {
                SwitchPreference(title = "隐藏不及格成绩",
                    checked = AppConfig.hideFailExam,
                    onCheckedChange = {
                        AppConfig.hideFailExam = it
                        AppConfig.save()
                    }
                )
            }*/

            SmallTitle("教务系统")
            Card(modifier = Modifier.padding(cardPadding)) {
                val serverItems = listOf(
                    "http://61.187.179.66:8924/",
                    "http://jwzx.usc.edu.cn:8924/"
                )
                var selectedServer by remember { mutableIntStateOf(0) }
                selectedServer = if (AppConfig.serverUrl.equals(serverItems[1])) 1 else 0
                WindowDropdownPreference(
                    title = "服务器",
                    items = serverItems,
                    selectedIndex = selectedServer,
                    onSelectedIndexChange = {
                        if (it != selectedServer) {
                            selectedServer = it
                            AppConfig.serverUrl = serverItems[it]
                            AppConfig.save()
                        }
                    }
                )
            }

            SmallTitle("关于")
            Card(modifier = Modifier.padding(cardPadding)) {
                BasicComponent (
                    title = "操作系统",
                    endActions = {
                        Text(getPlatform().name)
                    }
                )
                ArrowPreference(title = "开放源代码许可",
                    onClick = {
                        val html = ResourceUtils.readText("open-source-license.html")?:"Can not find the file!"
                        navigator.push(Route.SimpleHtml(html,"Open Source License of Pumpkin Toolkit"))
                    }
                )
                BasicComponent(
                    title = "问题反馈",
                    endActions = {
                        Text("nggjx@pgigi.com")
                    },
                    onClick = {
                        uriHandler.openUri("mailto:nggjx@pgigi.com")
                    }
                )
                BasicComponent(
                    title = "问题反馈",
                    endActions = {
                        Text("pumpkintoolkit@pgigi.com")
                    },
                    onClick = {
                        uriHandler.openUri("mailto:pumpkintoolkit@pgigi.com")
                    }
                )
            }
        }
        WindowDialog(title = "请选择开课时间", show = showDialog.value, onDismissRequest = { showDialog.value = false }) {
            val dismiss = LocalDismissState.current
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val numberDatePickerState = rememberNumberDatePickerState(if(AppConfig.startDate==null) localDate else AppConfig.startDate!!)
                Text(text = "请注意: 星期日算一周的第一天!")
                NumberDatePicker(
                    numberDatePickerState = numberDatePickerState,
                    start = LocalDate(localDate.year-1, 1, 1),
                    end = LocalDate(localDate.year+1, 12, 31)
                )

                val dayList = listOf("一", "二", "三", "四", "五", "六", "日")
                Text("若${numberDatePickerState.year}/${numberDatePickerState.month}/${numberDatePickerState.day}" +
                        "(周${dayList[LocalDate(numberDatePickerState.year, numberDatePickerState.month, numberDatePickerState.day).dayOfWeek.ordinal]})开课, " +
                        "则本周为第${
                            WeekCalculator(
                                LocalDate(
                                    numberDatePickerState.year,
                                    numberDatePickerState.month,
                                    numberDatePickerState.day
                                ), 1
                            ).getWeekNumber(localDate)}周")
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
                            AppConfig.startDate = LocalDate(numberDatePickerState.year, numberDatePickerState.month, numberDatePickerState.day)
                            AppConfig.save()
                            dismiss?.invoke()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}