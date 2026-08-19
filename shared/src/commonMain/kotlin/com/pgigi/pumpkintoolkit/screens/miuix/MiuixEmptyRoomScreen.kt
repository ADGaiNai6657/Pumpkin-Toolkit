package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.components.miuix.NumberDatePicker
import com.pgigi.pumpkintoolkit.components.NumberDatePickerState
import com.pgigi.pumpkintoolkit.components.rememberNumberDatePickerState
import com.pgigi.pumpkintoolkit.utils.JsonUtil
import com.pgigi.pumpkintoolkit.utils.PinyinUtils
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.utils.ResourceUtils
import com.pgigi.pumpkintoolkit.utils.WeekCalculator
import com.pgigi.pumpkintoolkit.viewmodel.EmptyRoomViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextButtonColors
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.time.Clock

@Serializable
data class Building(
    var name: String = "",
    var value : String = "",
    var school : String = ""
)

data class Room(
    var name: String = "",
    var empty: Boolean = false
)

@Composable
fun ResultCard(modifier: Modifier = Modifier, title: String="", empty: Boolean=false){
    Card(
        modifier = modifier
            .fillMaxWidth()
//                .padding(vertical = 4.dp)
    ) {
        BasicComponent(
            title = title,
            endActions = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (empty) "教务系统无课" else "有课",
                        fontSize = MiuixTheme.textStyles.headline1.fontSize,
                        textAlign = TextAlign.Center,
                        color = if (empty) MiuixTheme.colorScheme.onSecondaryVariant else MiuixTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

val dialogShow =  mutableStateOf(false)

val schoolItems = listOf("红湘","雨母","校外")

val lessonItems = listOf("第1、2节","第3、4节","第5、6节","第7、8节", "第9、10节")

val displayList = mutableStateListOf<Room>()
var isLoading by  mutableStateOf(false)

val schoolMapping = mapOf(
    0 to "1",
    1 to "2",
    2 to "3"
)

fun sortList(viewModel: EmptyRoomViewModel) {
    val list = mutableListOf<Room>()
    viewModel.emptyRoomMap.forEach { (key, value) ->
        list.add(Room(key, value))
    }
    displayList.clear()
    displayList.addAll(list.sortedBy { PinyinUtils.toPinyin(it.name) }.sortedByDescending { it.empty })
}

@Composable
fun Operations(modifier: Modifier = Modifier, viewModel: EmptyRoomViewModel, numberDatePickerState: NumberDatePickerState, snackbarHostState: SnackbarHostState, screenWidthDp: Dp, onQuerySuccess: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var showTermSection by remember { mutableStateOf(AppConfig.startDate == null) }
    var showStartDateDialog by remember { mutableStateOf(false) }
    val startDatePickerState = rememberNumberDatePickerState()
    var isFetchingStartDate by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.startDate) {
        if (viewModel.startDate == null) showTermSection = true
    }

    LaunchedEffect(viewModel.selectedTermIndex) {
        if (viewModel.selectedTermIndex < 0) return@LaunchedEffect
        if (AppConfig.termValueList.isEmpty()) return@LaunchedEffect
        val termId = AppConfig.termValueList[viewModel.selectedTermIndex]
        val cached = viewModel.termStartDates[termId]
        if (cached != null) {
            viewModel.startDate = cached
            return@LaunchedEffect
        }
        if (viewModel.startDate != null) {
            viewModel.termStartDates[termId] = viewModel.startDate!!
            return@LaunchedEffect
        }
        isFetchingStartDate = true
        val fetched = QZClient.getStartDate(termId)
        isFetchingStartDate = false
        if (fetched != null) {
            viewModel.startDate = fetched
            viewModel.termStartDates[termId] = fetched
        } else {
            viewModel.startDate = null
            showTermSection = true
        }
    }

    Column(modifier = modifier) {
        WindowDropdownPreference(
            title = "校区",
            items = schoolItems,
            selectedIndex = viewModel.selectedSchool,
            onSelectedIndexChange = {
                viewModel.selectedSchool = it
            }
        )
        WindowDropdownPreference(
            title = "教学楼",
            items = viewModel.buildingItems.toList(),
            selectedIndex = viewModel.selectedBuilding,
            onSelectedIndexChange = {
                viewModel.selectedBuilding = it
            }
        )
        val dayList = listOf("一", "二", "三", "四", "五", "六", "日")
        ArrowPreference(
            title = "日期",
            onClick = {
                dialogShow.value = true
            },
            endActions = {
                val localDate = LocalDate(
                    numberDatePickerState.year,
                    numberDatePickerState.month,
                    numberDatePickerState.day
                )
                Text(
                    text = "${localDate}(周${
                        dayList[LocalDate(
                            numberDatePickerState.year,
                            numberDatePickerState.month,
                            numberDatePickerState.day
                        ).dayOfWeek.ordinal]
                    })",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    textAlign = TextAlign.End,
                )
            }
        )
        WindowDropdownPreference(
            title = "节次",
            items = lessonItems,
            selectedIndex = viewModel.selectedLesson,
            onSelectedIndexChange = {
                viewModel.selectedLesson = it
            }
        )
        ArrowPreference(
            title = "学期及开课日期",
            onClick = { showTermSection = !showTermSection },
            endActions = {
                Text(
                    text = if(showTermSection) "点击收起设置" else "点击展开设置",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    textAlign = TextAlign.End,
                )
            }
        )
        AnimatedVisibility(showTermSection) {
            Column {
                WindowDropdownPreference(
                    title = "学期",
                    items = if (AppConfig.termNameList.isEmpty()) listOf("请先登录")
                           else AppConfig.termNameList.toList(),
                    selectedIndex = viewModel.selectedTermIndex.coerceAtLeast(0),
                    onSelectedIndexChange = { viewModel.selectedTermIndex = it }
                )
                ArrowPreference(
                    title = "开课日期",
                    onClick = {
                        viewModel.startDate?.let {
                            startDatePickerState.year = it.year
                            startDatePickerState.month = it.month.number
                            startDatePickerState.day = it.day
                        }
                        showStartDateDialog = true
                    },
                    endActions = {
                        Text(
                            text = viewModel.startDate?.toString() ?: "点击设置",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            textAlign = TextAlign.End,
                        )
                    }
                )
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColorsPrimary(),
            onClick = {
                val termId = if (viewModel.selectedTermIndex in AppConfig.termValueList.indices) {
                    AppConfig.termValueList[viewModel.selectedTermIndex]
                } else AppConfig.defaultTermId

                if (termId.isEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = "请选择学期", withDismissAction = true)
                    }
                    return@Button
                }
                if (viewModel.startDate == null) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = "请设置开课日期", withDismissAction = true)
                    }
                    return@Button
                }
                if (viewModel.buildingList[viewModel.selectedBuilding].isEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = "请选择教学楼", withDismissAction = true)
                    }
                    return@Button
                }
                isLoading = true
                coroutineScope.launch {
                    viewModel.emptyRoomMap.clear()
                    val queryDate = LocalDate(
                        numberDatePickerState.year,
                        numberDatePickerState.month,
                        numberDatePickerState.day
                    )
                    val weekCalculator =
                        WeekCalculator(viewModel.startDate!!, 1)

                    val emptyRooms = QZClient.getEmptyRooms(
                        termId,
                        viewModel.buildingList[viewModel.selectedBuilding],
                        queryDate.dayOfWeek.isoDayNumber,
                        weekCalculator.getWeekNumber(queryDate).toInt(),
                        viewModel.selectedLesson * 2 + 1
                    )
                    if (emptyRooms != null) {
                        viewModel.emptyRoomMap.putAll(emptyRooms)
                        sortList(viewModel = viewModel)
                        isLoading = false
                        onQuerySuccess()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message = "查询完成", withDismissAction = true)
                        }
                    } else {
                        isLoading = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message = "查询失败，请检查网络或教务系统", withDismissAction = true)
                        }
                    }
                }
            }
        ) {
            if (!isLoading) Text(
                text = "查询",
                color = MiuixTheme.colorScheme.onPrimary
            )
            if (isLoading) InfiniteProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(22.5.dp),
                color = MiuixTheme.colorScheme.disabledOnPrimary
            )
        }
    }

    WindowDialog(
        title = "请选择开课日期",
        show = showStartDateDialog,
        onDismissRequest = { showStartDateDialog = false },
    ) {
        val dismiss = LocalDismissState.current
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            NumberDatePicker(
                numberDatePickerState = startDatePickerState,
                start = LocalDate(todayDate.year - 1, 1, 1),
                end = LocalDate(todayDate.year + 1, 12, 31)
            )
            TextButton(
                text = "确定",
                colors = TextButtonColors(
                    MiuixTheme.colorScheme.primary,
                    MiuixTheme.colorScheme.disabledPrimary,
                    MiuixTheme.colorScheme.onPrimary,
                    MiuixTheme.colorScheme.disabledOnPrimary
                ),
                onClick = {
                    viewModel.startDate = LocalDate(
                        startDatePickerState.year,
                        startDatePickerState.month,
                        startDatePickerState.day
                    )
                    if (viewModel.selectedTermIndex in AppConfig.termValueList.indices) {
                        val termId = AppConfig.termValueList[viewModel.selectedTermIndex]
                        viewModel.termStartDates[termId] = viewModel.startDate!!
                    }
                    showStartDateDialog = false
                    dismiss?.invoke()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ResultList(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(displayList.size + 1) { index ->
            if (index < displayList.size) {
                ResultCard(
                    title = displayList[index].name,
                    empty = displayList[index].empty,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Text(
                    text = "本工具需要程序获取到正确学期、且正确设置开学日期的情况下才能正常使用。\n" +
                            "结果默认以中文顺序显示，教务系统无课程的教室优先显示。\n" +
                            "本工具仅是将教务系统返回的教室课程信息进行解析，不保证空教室信息正确性。",
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun MiuixEmptyRoomScreen(viewModel: EmptyRoomViewModel = viewModel(factory = EmptyRoomViewModel.Factory)) {
    val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val numberDatePickerState = rememberNumberDatePickerState()
    val navigator = LocalNavigator.current
    val windowInfo = LocalWindowInfo.current
    var showOperations by remember { mutableStateOf(true) }
    val snackbarHostState = remember{ SnackbarHostState() }

    LaunchedEffect(Unit) {
        val json = ResourceUtils.readText("buildings.json")
        json?.let{
            val list = JsonUtil.parseListJson(json, Building.serializer())
            if (list.isNotEmpty()) {
                viewModel.buildings.clear()
                viewModel.buildings.addAll(list)
                viewModel.buildingItems.clear()
                viewModel.buildingList.clear()
                val filteredBuildings =
                    viewModel.buildings.filter { it.school == schoolMapping[viewModel.selectedSchool] }
                for (building in filteredBuildings) {
                    viewModel.buildingItems.add(building.name)
                    viewModel.buildingList.add(building.value)
                }
            }
        }
        if (AppConfig.startDate != null) {
            viewModel.startDate = AppConfig.startDate
        }
        if (AppConfig.defaultTermId.isNotEmpty()) {
            val index = AppConfig.termValueList.indexOf(AppConfig.defaultTermId)
            if (index >= 0) {
                viewModel.selectedTermIndex = index
            }
        }
    }

    LaunchedEffect(viewModel.selectedSchool) {
        if (viewModel.buildings.isEmpty()) return@LaunchedEffect
        viewModel.buildingItems.clear()
        viewModel.buildingList.clear()
        val filteredBuildings =
            viewModel.buildings.filter { it.school == schoolMapping[viewModel.selectedSchool] }
        for (building in filteredBuildings) {
            viewModel.buildingItems.add(building.name)
            viewModel.buildingList.add(building.value)
        }
        viewModel.selectedBuilding = 0
    }

    Scaffold(
        snackbarHost = {SnackbarHost(snackbarHostState)},
        topBar = {
            SmallTopAppBar(
                title = "空教室查询",
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    if (windowInfo.containerDpSize.width <= 800.dp) {
                        AnimatedVisibility(visible = showOperations){
                            IconButton(onClick = { showOperations = !showOperations }) {
                                Icon(
                                    imageVector = MiuixIcons.ExpandLess,
                                    contentDescription = "隐藏查询操作台"
                                )
                            }
                        }
                        AnimatedVisibility(visible = !showOperations){
                            IconButton(onClick = { showOperations = !showOperations }) {
                                Icon(
                                    imageVector = MiuixIcons.ExpandMore,
                                    contentDescription = "显示查询操作台"
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        if (windowInfo.containerDpSize.width > 800.dp) {
            Row(
                modifier = Modifier.padding(paddingValues)
            ) {
                Operations(Modifier.width(400.dp), viewModel, numberDatePickerState, snackbarHostState = snackbarHostState,windowInfo.containerDpSize.width) { }
                ResultList(Modifier.fillMaxSize())
            }
        } else {
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                AnimatedVisibility(showOperations) {
                    Operations(
                        viewModel = viewModel,
                        numberDatePickerState = numberDatePickerState,
                        screenWidthDp = windowInfo.containerDpSize.width,
                        snackbarHostState = snackbarHostState,
                        onQuerySuccess = {
                            // 窄屏时查询成功后隐藏操作台
                            if (windowInfo.containerDpSize.width <= 800.dp) {
                                showOperations = false
                            }
                        }
                    )
                }
                ResultList()
            }
        }
    }
    val dayList = listOf("一", "二", "三", "四", "五", "六", "日")
    WindowDialog(
        title = "请选择日期",
        show = dialogShow.value,
        onDismissRequest = { dialogShow.value = false },
    ) {
        val dismiss = LocalDismissState.current
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            NumberDatePicker(
                numberDatePickerState = numberDatePickerState,
                start = LocalDate(localDate.year-1, 1, 1),
                end = LocalDate(localDate.year+1, 12, 31)
            )
            TextButton(
                text = "确定(周${
                    dayList[LocalDate(
                        numberDatePickerState.year,
                        numberDatePickerState.month,
                        numberDatePickerState.day
                    ).dayOfWeek.ordinal]
                })",
                colors = TextButtonColors(
                    MiuixTheme.colorScheme.primary,
                    MiuixTheme.colorScheme.disabledPrimary,
                    MiuixTheme.colorScheme.onPrimary,
                    MiuixTheme.colorScheme.disabledOnPrimary
                ),
                onClick = { dismiss?.invoke() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            /*Row {
                TextButton(
                    text = "关闭",
                    onClick = { dismiss?.invoke() },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            }*/
        }
    }
}