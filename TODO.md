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
var tomorrowScheduleEnabled by mutableStateOf(false)   // 开关，默认关
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
tomorrowScheduleEnabled = kvault.getBoolean(KEY.TOMORROW_ENABLED) ?: false
tomorrowSwitchHour = kvault.getInt(KEY.TOMORROW_SWITCH_HOUR) ?: 22
tomorrowSwitchMinute = kvault.getInt(KEY.TOMORROW_SWITCH_MINUTE) ?: 0
```

### 1.4 在 `save()` 里写入
- [ ] 找到 `save()`，加入：

```kotlin
kvault.putBoolean(KEY.TOMORROW_ENABLED, tomorrowScheduleEnabled)
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
    checked = AppConfig.tomorrowScheduleEnabled,
    onCheckedChange = {
        AppConfig.tomorrowScheduleEnabled = it
        AppConfig.save()
    }
)
```

### 2.2 加一个"切换时间"行
- [ ] 紧跟在开关后面加（`ArrowPreference` 右侧显示时间）：

```kotlin
ArrowPreference(
    title = "切换时间",
    endActions = {
        Text(
            text = "%02d:%02d".format(AppConfig.tomorrowSwitchHour, AppConfig.tomorrowSwitchMinute),
            modifier = Modifier.align(Alignment.CenterVertically),
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
        )
    },
    onClick = { showTimeDialog.value = true }
)
```

- [ ] 在函数开头声明弹窗开关和临时值（`remember` 表示页面重建时记住）：

```kotlin
val showTimeDialog = remember { mutableStateOf(false) }
var tempHour by remember { mutableIntStateOf(AppConfig.tomorrowSwitchHour) }
var tempMinute by remember { mutableIntStateOf(AppConfig.tomorrowSwitchMinute) }
```

> `mutableStateOf` / `mutableIntStateOf`、`remember` 忘了就翻《零基础导读》第 2 章。

### 2.3 加滚轮弹窗
- [ ] 在文件末尾，仿照「开课时间」弹窗写一个（`NumberPicker` 需要 `import top.yukonga.miuix.kmp.basic.NumberPicker`）：

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
            Text(":", fontWeight = FontWeight.Bold)
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

### 2.4 验证
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
            checked = AppConfig.tomorrowScheduleEnabled,
            onCheckedChange = {
                AppConfig.tomorrowScheduleEnabled = it
                AppConfig.save()
            }
        )
    },
    onClick = {
        AppConfig.tomorrowScheduleEnabled = !AppConfig.tomorrowScheduleEnabled
        AppConfig.save()
    },
)
```

### 3.2 加时间行
- [ ] 加：

```kotlin
M3Row(
    title = "切换时间",
    trailingContent = {
        M3TrailingText("%02d:%02d".format(AppConfig.tomorrowSwitchHour, AppConfig.tomorrowSwitchMinute))
    },
    onClick = { showTimeDialog.value = true },
)
```
- [ ] 同样在开头声明 `showTimeDialog` / `tempHour` / `tempMinute`。

### 3.3 加弹窗（注意颜色！）
- [ ] 用 `AlertDialog`（参考文件里已有的日期弹窗），里面放两个 `NumberPicker`。
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
- [ ] 去「设置 → UI 风格」切到 Material 3，重复阶段 2.4 的验证。

---

## 阶段 4：写"显示今天还是明天"的判断（新建 Resolver）

> 目标：写一个纯函数：给时间和课程，返回"标题 + 要显示的课 + 是否假期"。

### 4.1 新建文件
- [ ] 新建 `shared/src/commonMain/.../utils/TodayScheduleResolver.kt`。

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

## 阶段 5：接到「今日课程」页 + 自动刷新

> 目标：让页面用 Resolver 的结果显示课程。

### 5.1 改 `TodayScreen.kt`（Miuix）
- [ ] 在函数里加一个状态和一段定时逻辑（放在原来算 `todayCourses` 的地方）：

```kotlin
var viewState by remember {
    mutableStateOf(
        resolveTodayView(
            now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            courseList = viewModel.courseList,
            startDate = AppConfig.startDate,
            currentWeek = viewModel.currentWeek,
            totalWeek = AppConfig.totalWeek,
            enabled = AppConfig.tomorrowScheduleEnabled,
            switchHour = AppConfig.tomorrowSwitchHour,
            switchMinute = AppConfig.tomorrowSwitchMinute
        )
    )
}

LaunchedEffect(AppConfig.tomorrowScheduleEnabled, AppConfig.tomorrowSwitchHour,
               AppConfig.tomorrowSwitchMinute, viewModel.courseList.size) {
    while (true) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        viewState = resolveTodayView(
            now, viewModel.courseList, AppConfig.startDate, viewModel.currentWeek,
            AppConfig.totalWeek, AppConfig.tomorrowScheduleEnabled,
            AppConfig.tomorrowSwitchHour, AppConfig.tomorrowSwitchMinute
        )
        delay(30_000)   // 每 30 秒重新判断一次
    }
}
```

- [ ] 原来那两行（算 `weekCourses` / `todayCourses`）**删掉或改掉**。
- [ ] 把标题 `"今日课程"` 换成 `viewState.title`。
- [ ] 把列表数据 `todayCourses` 换成 `viewState.courses`。
- [ ] 假期时显示「假期中」：判断 `viewState.isHoliday`。

### 5.2 改 `Material3TodayScreen.kt`
- [ ] 做和 5.1 一模一样的改动（两套 UI 逻辑要一致）。

### 5.3 验证
- [ ] 进设置，把时间设成「当前时间 + 1 分钟」，回到「今日课程」页等一分钟，看标题是否自动变成「明日课程」、课是否变成明天的。

---

## 阶段 6：边界情况（做完 5 再逐条检查）

- [ ] **跨周**：周六晚上切换，明天是周日，属于下一周。确认显示的是下一周的课（Resolver 已用 `targetDate` 重算周次）。
- [ ] **假期**：第 0 周 / 超过总周数时显示「假期中」，不崩溃。
- [ ] **不要用 `AppConfig.localDate`**：它是 App 启动时算死的，跨天会过期。一律用实时的 `Clock.System.now()`。
- [ ] **`startDate` 为空**：降级用 `viewModel.currentWeek`，不崩溃。
- [ ] **未登录**：仍显示「请登录使用」。

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
  → 每 30 秒重算一次，保证时间一过就自动切换
```
