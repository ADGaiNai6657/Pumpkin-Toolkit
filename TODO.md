# TODO（新手详细版）：晚间自动显示明日课程

> 目标：过了你设置的时间点后，App 的「今日课程」页自动改成显示明天课表。
> 范围：先只做 App 内页面；桌面小组件不管。切换时间做成设置项，用滚轮选时/分。
> 配套：《零基础导读.md》第 1 章（Kotlin）、第 2 章（Compose）、第 5.6 节（TodayScreen）。

---

## 开始之前，先记住三件事

1. **一次只做一小步**，每做完一步就编译一次。不要一口气全改完。
2. **怎么算"做对了"**：能编译过（Build 成功）或运行后看到预期效果。每步下面都写了「怎么验证」。
3. **卡住了**：把那一小段代码 + 报错信息发给 AI，说"我是 Java 基础，逐行解释"。

### 涉及的文件（先混个脸熟）
| 文件 | 作用 |
|---|---|
| `shared/src/commonMain/.../AppConfig.kt` | 存设置（开关、时间） |
| `shared/src/commonMain/.../screens/miuix/MiuxSettingScreen.kt` | Miuix 设置页 |
| `shared/src/commonMain/.../screens/material3/Material3SettingScreen.kt` | M3 设置页 |
| `shared/src/commonMain/.../screens/miuix/TodayScreen.kt` | Miuix 今日课程页（主战场） |
| `shared/src/commonMain/.../screens/material3/Material3TodayScreen.kt` | M3 今日课程页 |
| `shared/src/commonMain/.../utils/TodayScheduleResolver.kt` | 新建：判断"显示今天还是明天" |

---

## 阶段 1：让 App 能记住"开关"和"时间"（改 AppConfig）

> 目标：先不碰界面，只在配置里加 3 个记忆点。做完这一阶段，配置能存能读。
> 参考：《零基础导读》第 4.2 节、`AppConfig.kt` 里的 `timeSeason`（照抄它）。

### 1.1 加 3 个字段
- [ ] 打开 `AppConfig.kt`，在课表相关字段附近（`timeSeason` 那一堆）加入：

```kotlin
var tomorrowScheduleEnable by mutableStateOf(false)   // 开关，默认关
var tomorrowSwitchHour by mutableIntStateOf(22)         // 几点（0-23），默认 22
var tomorrowSwitchMinute by mutableIntStateOf(0)        // 几分（0-59），默认 0
```

### 1.2 加 3 个 KEY 常量
- [ ] 在同文件的 `object KEY { ... }` 里加：

```kotlin
const val TOMORROW_ENABLED = "tomorrow_enabled"
const val TOMORROW_SWITCH_HOUR = "tomorrow_switch_hour"
const val TOMORROW_SWITCH_MINUTE = "tomorrow_switch_minute"
```

### 1.3 在 `load()` 里读取
- [ ] 找到 `load()`，加入（`?:` 表示读不到就用默认值）：

```kotlin
tomorrowScheduleEnable = kvault.getBoolean(KEY.TOMORROW_ENABLED) ?: false
tomorrowSwitchHour = kvault.getInt(KEY.TOMORROW_SWITCH_HOUR) ?: 22
tomorrowSwitchMinute = kvault.getInt(KEY.TOMORROW_SWITCH_MINUTE) ?: 0
```

### 1.4 在 `save()` 里写入
- [ ] 找到 `save()`，加入：

```kotlin
kvault.putBoolean(KEY.TOMORROW_ENABLED, tomorrowScheduleEnable)
kvault.putInt(KEY.TOMORROW_SWITCH_HOUR, tomorrowSwitchHour)
kvault.putInt(KEY.TOMORROW_SWITCH_MINUTE, tomorrowSwitchMinute)
```

### 1.5 验证
- [ ] 编译（Build → Make Project）。没有红线 / 没有报错就对了。

---

## 阶段 2：Miuix 设置页（开关 + 时间行 + 滚轮弹窗）

> 目标：在 Miuix 设置页能开关它、能点开滚轮选时间。
> 参考：`MiuxSettingScreen.kt` 里「课表设置」卡片，以及文件末尾「开课时间」弹窗（约 `:422`）。

### 2.1 加一个开关
- [ ] 在「课表设置」的 `Card { ... }` 里加：

```kotlin
SwitchPreference(
    title = "晚间自动显示明日课程",
    checked = AppConfig.tomorrowScheduleEnable,
    onCheckedChange = {
        AppConfig.tomorrowScheduleEnable = it
        AppConfig.save()
    }
)
```

### 2.2 先在函数开头声明弹窗状态（重要！）
- [ ] 在 `MiuixSettingScreen()` 的**最上面**（`Scaffold` 之前）声明这三样。**必须在引用之前声明**，否则会报 `Unresolved reference: showTimeDialog`：

```kotlin
val showTimeDialog = remember { mutableStateOf(false) }           // 弹窗开/关
var tempHour by remember { mutableIntStateOf(AppConfig.tomorrowSwitchHour) }
var tempMinute by remember { mutableIntStateOf(AppConfig.tomorrowSwitchMinute) }
```

> `mutableStateOf` / `mutableIntStateOf`、`remember` 忘了就翻《零基础导读》第 2 章。

### 2.3 加一个"切换时间"行
- [ ] 紧跟在开关后面加（`ArrowPreference` 右侧显示时间）：

```kotlin
ArrowPreference(
    title = "切换时间",
    endActions = {
        Text(
            // ⚠️ 不要用 String.format（JVM 专属，iOS 会编译失败）。用 padStart。
            text = "${AppConfig.tomorrowSwitchHour.toString().padStart(2, '0')}:" +
                   "${AppConfig.tomorrowSwitchMinute.toString().padStart(2, '0')}",
            modifier = Modifier.align(Alignment.CenterVertically),
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
        )
    },
    onClick = { showTimeDialog.value = true }
)
```

### 2.4 加滚轮弹窗
- [ ] 在文件末尾，仿照「开课时间」弹窗写一个：

```kotlin
// 需要这两个 import（放文件顶部）：
import top.yukonga.miuix.kmp.basic.NumberPicker
```

```kotlin
WindowDialog(
    title = "选择切换时间",
    show = showTimeDialog.value,
    onDismissRequest = { showTimeDialog.value = false }
) {
    val dismiss = LocalDismissState.current
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberPicker(
                value = tempHour,
                onValueChange = { tempHour = it },
                range = 0..23,
                label = { it.toString().padStart(2, '0') },
                wrapAround = true,
                modifier = Modifier.weight(1f)
            )
            Text(":")   // 想加粗的话：Text(":", fontWeight = FontWeight.Bold)，并 import androidx.compose.ui.text.font.FontWeight
            NumberPicker(
                value = tempMinute,
                onValueChange = { tempMinute = it },
                range = 0..59,
                label = { it.toString().padStart(2, '0') },
                wrapAround = true,
                modifier = Modifier.weight(1f)
            )
        }
        Row {
            TextButton(text = "取消", onClick = { dismiss?.invoke() }, modifier = Modifier.weight(1f).padding(8.dp))
            TextButton(
                text = "确认",
                onClick = {
                    AppConfig.tomorrowSwitchHour = tempHour
                    AppConfig.tomorrowSwitchMinute = tempMinute
                    AppConfig.save()
                    dismiss?.invoke()
                },
                modifier = Modifier.weight(1f).padding(8.dp)
            )
        }
    }
}
```

### 2.5 验证
- [ ] 运行 App，进入「设置 → 课表设置」：能看到开关和时间行。
- [ ] 点时间行 → 出现滚轮 → 选 21:30 → 确认 → 时间行变成 `21:30`。
- [ ] 杀掉 App 再打开，设置还在（说明存住了）。

---

## 阶段 3：Material 3 设置页（同样的东西）

> 目标：M3 风格下也能开关和选时间。
> 参考：`Material3SettingScreen.kt` 的 `M3GroupHeader("课表设置")` 和文件末尾的 `AlertDialog`。

### 3.1 加开关行
- [ ] 在「课表设置」分组里加：

```kotlin
M3Row(
    title = "晚间自动显示明日课程",
    trailingContent = {
        Switch(
            checked = AppConfig.tomorrowScheduleEnable,
            onCheckedChange = {
                AppConfig.tomorrowScheduleEnable = it
                AppConfig.save()
            }
        )
    },
    onClick = {
        AppConfig.tomorrowScheduleEnable = !AppConfig.tomorrowScheduleEnable
        AppConfig.save()
    },
)
```

### 3.2 声明弹窗状态 + 加时间行
- [ ] 先在 `Material3SettingScreen()` 最上面声明（和 Miuix 一样，别漏）：

```kotlin
val showTimeDialog = remember { mutableStateOf(false) }
var tempHour by remember { mutableIntStateOf(AppConfig.tomorrowSwitchHour) }
var tempMinute by remember { mutableIntStateOf(AppConfig.tomorrowSwitchMinute) }
```

- [ ] 再加时间行：

```kotlin
M3Row(
    title = "切换时间",
    trailingContent = {
        M3TrailingText(
            "${AppConfig.tomorrowSwitchHour.toString().padStart(2, '0')}:" +
            "${AppConfig.tomorrowSwitchMinute.toString().padStart(2, '0')}"
        )
    },
    onClick = { showTimeDialog.value = true },
)
```

### 3.3 加弹窗（注意颜色！）
- [ ] 用 `AlertDialog`（参考文件里已有的日期弹窗），里面放两个 `NumberPicker`。
- [ ] 需要这两个 import（放文件顶部）：

```kotlin
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.NumberPickerDefaults
```

- [ ] ⚠️ M3 分支没有 `MiuixTheme`，所以**必须显式给颜色和文字样式**，否则可能取不到默认值：

```kotlin
NumberPicker(
    value = tempHour,
    onValueChange = { tempHour = it },
    range = 0..23,
    label = { it.toString().padStart(2, '0') },
    wrapAround = true,
    colors = NumberPickerDefaults.colors(
        selectedTextColor = MaterialTheme.colorScheme.onSurface,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSelectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledUnselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
    ),
    textStyle = MaterialTheme.typography.titleLarge,
    modifier = Modifier.weight(1f)
)
```

### 3.4 验证
- [ ] 去「设置 → UI 风格」切到 Material 3，重复阶段 2.5 的验证。

---

## 阶段 4：写"显示今天还是明天"的判断（新建 Resolver）

> 目标：写一个纯函数：给时间和课程，返回"标题 + 要显示的课 + 是否假期"。

### 4.1 新建文件
- [ ] 新建 `shared/src/commonMain/.../utils/TodayScheduleResolver.kt`。
- [ ] 文件顶部需要这些 import（其余交给 IDE 自动补全）：

```kotlin
import com.pgigi.pumpkintoolkit.models.Course
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
// WeekCalculator 和 buildWeekCourses 与本文件同属 utils 包，不用 import
```

### 4.2 定义返回类型
- [ ] 写：

```kotlin
data class TodayViewState(
    val title: String,          // "今日课程" 或 "明日课程"
    val courses: List<Course>,  // 要显示的课
    val isHoliday: Boolean      // 是否假期
)
```

### 4.3 写函数（一行一行照抄理解）
- [ ] 写：

```kotlin
fun resolveTodayView(
    now: LocalDateTime,
    courseList: List<Course>,
    startDate: LocalDate?,
    currentWeek: Int,
    totalWeek: Int,
    enabled: Boolean,
    switchHour: Int,
    switchMinute: Int
): TodayViewState {
    // 1) 现在过了切换时间点吗？（都换算成"分钟"再比大小）
    val nowMinutes = now.hour * 60 + now.minute
    val switchPassed = enabled && nowMinutes >= switchHour * 60 + switchMinute

    // 2) 决定目标日期：过了就取明天，否则今天
    val targetDate = if (switchPassed) now.date.plus(1, DateTimeUnit.DAY) else now.date

    // 3) 目标日期是第几周？（明天可能跨周，必须用 targetDate 重算！）
    val targetWeek = startDate?.let {
        WeekCalculator(it, 1).getWeekNumber(targetDate).toInt()
    } ?: currentWeek

    // 4) 判断假期
    val isHoliday = targetWeek <= 0 || (totalWeek > 0 && targetWeek > totalWeek)
    val title = if (switchPassed) "明日课程" else "今日课程"

    if (isHoliday) return TodayViewState(title, emptyList(), true)

    // 5) 取出那一周的课，再筛出星期几对应的课
    val weekCourses = buildWeekCourses(courseList).getOrElse(targetWeek - 1) { emptyList() }
    val courses = weekCourses.filter {
        it.dayOfWeek == (targetDate.dayOfWeek.ordinal + 1) % 7
    }
    return TodayViewState(title, courses, false)
}
```

### 4.4 验证
- [ ] 编译通过。这个函数暂时还没人调用，没关系。

---

## 阶段 5：接到「今日课程」页 + 手动下拉刷新

> 目标：让 `TodayScreen`（Miuix）和 `Material3TodayScreen`（M3）用 `resolveTodayView` 显示结果。
> **刷新方式：用户手动下拉才刷新，不要自动刷新、不要死循环。**

### 5.0 现状
两个页面现在都是"自己算今天"（以 `TodayScreen.kt` 为例，约 `:53`、`:60-64`）：

```kotlin
val courses = buildWeekCourses(viewModel.courseList)                         // 所有周
// ...
val weekCourses = courses.getOrElse(viewModel.currentWeek - 1) { emptyList() }
val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
val todayCourses = weekCourses.filter { course ->
    course.dayOfWeek == (localDate.dayOfWeek.ordinal + 1) % 7
}
```
这几行要整体替换成"调用 resolver + 下拉刷新"。

---

### 5.1 改 `TodayScreen.kt`（Miuix）

#### 5.1.1 补 import
- [ ] 顶部加（`PullToRefresh`、`rememberPullToRefreshState`、`Clock`、`TimeZone`、`toLocalDateTime` 该文件已有）：

```kotlin
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.pgigi.pumpkintoolkit.utils.resolveTodayView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
```

#### 5.1.2 删掉旧取数，换成"状态 + 手动刷新"
- [ ] 删掉 `val courses = ...`、`val weekCourses = ...`、`val localDate = ...`、`val todayCourses = ...`（约 `:53`、`:60-64`）。
- [ ] 换成：

```kotlin
// 当前显示状态：进入页面时先算一次
var viewState by remember {
    mutableStateOf(
        resolveTodayView(
            now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            courseList = viewModel.courseList,
            startDate = AppConfig.startDate,
            currentWeek = viewModel.currentWeek,
            totalWeek = AppConfig.totalWeek,
            enabled = AppConfig.tomorrowScheduleEnable,
            switchHour = AppConfig.tomorrowSwitchHour,
            switchMinute = AppConfig.tomorrowSwitchMinute
        )
    )
}

// 手动刷新：按“此刻”重新算一次（只在用户下拉时调用）
val refreshToday = {
    viewState = resolveTodayView(
        now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        courseList = viewModel.courseList,
        startDate = AppConfig.startDate,
        currentWeek = viewModel.currentWeek,
        totalWeek = AppConfig.totalWeek,
        enabled = AppConfig.tomorrowScheduleEnable,
        switchHour = AppConfig.tomorrowSwitchHour,
        switchMinute = AppConfig.tomorrowSwitchMinute
    )
}

// 下拉刷新所需的状态
var isRefreshing by remember { mutableStateOf(false) }
val pullToRefreshState = rememberPullToRefreshState()
val scope = rememberCoroutineScope()
```

> ⚠️ 关键：**这里没有 `LaunchedEffect { while(true) ... }`，也没有 `delay(30_000)`**。页面不会自己刷新，必须用户下拉。

#### 5.1.3 用 `PullToRefresh` 包住列表
- [ ] 把原来的 `LazyColumn(...) { ... }` 整体包进 `PullToRefresh`：

```kotlin
PullToRefresh(
    isRefreshing = isRefreshing,
    onRefresh = {
        scope.launch {
            isRefreshing = true
            refreshToday()      // 下拉 → 手动刷新
            delay(300)          // 让指示器转一下（可去掉）
            isRefreshing = false
        }
    },
    pullToRefreshState = pullToRefreshState,
    modifier = Modifier.padding(paddingValues),
) {
    LazyColumn(state = listState) {
        items(viewState.courses.size) { index ->
            val course = viewState.courses[index]
            // ...原有卡片代码不变...
        }
        if (!loggedIn || viewState.courses.isEmpty()) {
            item {
                Text(
                    text = when {
                        !loggedIn -> "请登录使用"
                        viewState.isHoliday -> "假期中"
                        else -> "暂无课程"
                    },
                    // ...原有修饰符不变...
                )
            }
        }
        item { Spacer(modifier = Modifier.height(64.dp)) }
    }
}
```

> 注意：原来 `LazyColumn` 的 `Modifier.padding(paddingValues)` 移到 `PullToRefresh` 上，里面的 `LazyColumn` **不要再加**，否则双重留白。

#### 5.1.4 改标题
- [ ] `SmallTopAppBar(title = "今日课程", ...)` → `title = viewState.title`

---

### 5.2 改 `Material3TodayScreen.kt`（M3）
同样思路，M3 用 `PullToRefreshBox`。

#### 5.2.1 补 import
- [ ] 该文件原本缺少这些：

```kotlin
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.pgigi.pumpkintoolkit.utils.resolveTodayView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
```
（该文件已有 `@OptIn(ExperimentalMaterial3Api::class)`。若 `PullToRefreshBox` 报需要 OptIn，补上；若你的版本没有它，就退回"顶栏刷新按钮"，参考 `Material3ScheduleScreen.kt`。）

#### 5.2.2 状态 + 刷新函数 + 包列表
- [ ] 删掉 `val courses = ...`（约 `:51`）、`val weekCourses = ...`、`val localDate = ...`、`val todayCourses = ...`（约 `:59-63`）。
- [ ] 换成和 5.1.2 **一字不差**的状态与 `refreshToday`。
- [ ] 把 `LazyColumn(...)` 包进：

```kotlin
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = {
        scope.launch {
            isRefreshing = true
            refreshToday()
            delay(300)
            isRefreshing = false
        }
    },
    modifier = Modifier.padding(paddingValues),
) {
    LazyColumn(state = listState) { /* 同 5.1.3，把 todayCourses 换成 viewState.courses */ }
}
```

#### 5.2.3 改标题
- [ ] `title = { Text("今日课程") }` → `title = { Text(viewState.title) }`

---

### 5.3 验证
- [ ] 编译通过。
- [ ] 打开开关、时间设成「当前时间 + 1 分钟」，回到今日页后**什么都不做**：页面**不会自动**变化（已无自动刷新）。
- [ ] 这时**手动下拉**列表 → 刷新后标题变「明日课程」、课程变明天的。
- [ ] 关闭开关后下拉：仍显示「今日课程」。
- [ ] 假期周下拉 → 「假期中」；未登录 → 「请登录使用」。
- [ ] 两种 UI 风格表现一致。

### 5.4 常见坑
- 不要写 `LaunchedEffect { while(true) { ...; delay(30_000) } }`——那是自动刷新/死循环，本方案已移除。
- `viewState` 用 `remember` 只在进入页面时算一次，之后**只在下拉刷新时更新**。
- 报 `Unresolved reference: remember / rememberCoroutineScope / launch`：是漏了 5.1.1 / 5.2.1 的 import。

---

## 阶段 6：边界情况（做完 5 再逐条检查）

- [ ] **跨周**：周六晚上切换，明天是周日，属于下一周。确认显示的是下一周的课（Resolver 已用 `targetDate` 重算周次）。
- [ ] **假期**：第 0 周 / 超过总周数时显示「假期中」，不崩溃。
- [ ] **不要用 `AppConfig.localDate`**：它是 App 启动时算死的，跨天会过期。一律用实时的 `Clock.System.now()`。
- [ ] **`startDate` 为空**：降级用 `viewModel.currentWeek`，不崩溃。
- [ ] **未登录**：仍显示「请登录使用」。

---

## 常见报错对照表（改到一半报错先看这个）

| 报错信息 | 原因 | 解决 |
|---|---|---|
| `Unresolved reference: showTimeDialog` | 没声明，或声明写在了使用之后 | 在函数**最上面**声明 `val showTimeDialog = remember { mutableStateOf(false) }`（阶段 2.2 / 3.2） |
| `Unresolved reference: format`，或 iOS 编译失败 | 用了 JVM 专属的 `String.format` | 改用 `padStart(2, '0')` 拼字符串（见 2.3 / 3.2） |
| `Unresolved reference: FontWeight` | 用了 `FontWeight.Bold` 却没 import | 顶部加 `import androidx.compose.ui.text.font.FontWeight`，或去掉加粗 |
| `Unresolved reference: NumberPicker` / `NumberPickerDefaults` | 没 import | 加 `import top.yukonga.miuix.kmp.basic.NumberPicker`；用默认颜色再加 `import top.yukonga.miuix.kmp.basic.NumberPickerDefaults` |
| `Unresolved reference: resolveTodayView` | TodayScreen 没 import | 顶部加 `import com.pgigi.pumpkintoolkit.utils.resolveTodayView` |
| `Unresolved reference: tomorrowScheduleEnable` | AppConfig 字段没加，或两处拼写不一致 | 确认 AppConfig 与页面用的字段名完全一致 |
| 类型不匹配 / 参数报错 | 传入参数顺序或类型不对 | 对照阶段 4 的函数签名逐个核对 |

> 原则：报"找不到某个名字"的错，90% 是**没 import** 或**没声明/拼写不一致**。

---

## 附录 A：更改横竖屏逻辑（强制竖屏，取消横屏）

> 这条是你后加的。先确认要改的平台，再动手。

- [ ] 先决定范围：只 iOS？还是 Android + iOS 都要？（两平台改法完全不同）
- [ ] **Android**：在 `androidApp/src/main/AndroidManifest.xml` 的 `.MainActivity` 上加 `android:screenOrientation="portrait"`。
- [ ] **iOS**：在 `iosApp/iosApp/Info.plist` 里把 `UISupportedInterfaceOrientations` 只保留 `UIInterfaceOrientationPortrait`。
- [ ] 可选：`MiuixHomeScreen.kt` / `Material3HomeScreen.kt` 里有 `isLandscape` 判断（横屏用 `NavigationRail`），强制竖屏后可保留（不会触发）也可删除。
- [ ] 验证：旋转设备，界面不再横过来。

---

## 附录 B：一句话回顾整个功能

```
读配置（开关/时间）
  → 问：现在过点了吗？
     否 → 今天 + 当前周
     是 → 明天 + 明天所在周（可能跨周！）
  → 取出那一周的课，筛出目标星期几
  → 显示（标题随之切换）
  → 用户下拉刷新时重算（不自动切换）
```
