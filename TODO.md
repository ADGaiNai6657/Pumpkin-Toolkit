# TODO：晚间自动显示明日课程

> 需求：过了用户配置的时间点后，App 内「今日课程」页自动切换为显示明天课表。
> 范围：仅 iOS 端 App 内（忽略桌面小组件）。切换时间做成设置项，用滚轮选择时/分。
> 说明：相关页面在 `shared/commonMain`，改动会同时影响 Android（暂可接受）。

## 已确认的技术事实
- [ ] 项目 Miuix 版本 `0.9.4-rc01` 自带滚轮组件 `top.yukonga.miuix.kmp.basic.NumberPicker`
- [ ] 官方「时+分」双轮写法：`range = 0..23` / `0..59`，`label = { padStart(2,'0') }`，`wrapAround = true`
- [ ] `App.kt` 的 Material3 分支未包 `MiuixTheme`，M3 里用 `NumberPicker` 必须显式传 `colors` / `textStyle`

## 一、AppConfig（持久化设置项）
- [ ] 新增字段
  - [ ] `var tomorrowScheduleEnabled by mutableStateOf(false)`
  - [ ] `var tomorrowSwitchHour by mutableIntStateOf(22)`
  - [ ] `var tomorrowSwitchMinute by mutableIntStateOf(0)`
- [ ] 新增 KEY 常量：`TOMORROW_ENABLED` / `TOMORROW_SWITCH_HOUR` / `TOMORROW_SWITCH_MINUTE`
- [ ] `load()` 里读取（`getBoolean` / `getInt`，给默认值）
- [ ] `save()` 里写入（`putBoolean` / `putInt`）
- 参考：`AppConfig.kt:38`、`:76`、`:107`、`:142`

## 二、设置页 UI — Miuix
文件：`screens/miuix/MiuxSettingScreen.kt`（「课表设置」卡片，约 `:184`）
- [ ] `SwitchPreference("晚间自动显示明日课程")`，变更时 `AppConfig.save()`
- [ ] `ArrowPreference("切换时间")`，右侧显示 `String.format("%02d:%02d", hour, minute)`
- [ ] 点击打开 `WindowDialog`（结构照抄开课时间弹窗 `:422`）
- [ ] 弹窗内一行放两个 `NumberPicker`（0..23 / 0..59），中间 `Text(":")`
- [ ] 用临时 state 暂存选择，点「确认」才写回 `AppConfig` 并 `save()`，点「取消」丢弃

## 三、设置页 UI — Material 3
文件：`screens/material3/Material3SettingScreen.kt`（`M3GroupHeader("课表设置")`，约 `:231`）
- [ ] `M3Row("晚间自动显示明日课程")` + `Switch`
- [ ] `M3Row("切换时间")` + `M3TrailingText("22:00")`，点击打开 `AlertDialog`
- [ ] `AlertDialog` 内放同样的双 `NumberPicker`
- [ ] ⚠️ 显式传参，避免读取缺失的 `MiuixTheme` 默认值
  - [ ] `colors = NumberPickerDefaults.colors(selectedTextColor = MaterialTheme.colorScheme.onSurface, unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant, ...)`
  - [ ] `textStyle = MaterialTheme.typography.titleLarge`

## 四、共用解析逻辑（建议新增 `utils/TodayScheduleResolver.kt`）
- [ ] 写一个函数，输入 `now` / `courseList` / `startDate` / `enabled` / `hour` / `minute` / `totalWeek` / `currentWeek`
- [ ] 输出：`title` / `targetCourses` / `isHoliday`
- [ ] 逻辑：
  - [ ] `switchPassed = enabled && (now.hour*60 + now.minute) >= (hour*60 + minute)`
  - [ ] `targetDate = if (switchPassed) now.date + 1天 else now.date`
  - [ ] `targetWeek = startDate?.let { WeekCalculator(it, 1).getWeekNumber(targetDate) } ?: currentWeek`
  - [ ] `isHoliday = targetWeek <= 0 || (totalWeek > 0 && targetWeek > totalWeek)`
  - [ ] `list = buildWeekCourses(courseList).getOrElse(targetWeek - 1) { emptyList() }`
  - [ ] `targetCourses = list.filter { it.dayOfWeek == (targetDate.dayOfWeek.ordinal + 1) % 7 }`
  - [ ] `title = if (switchPassed) "明日课程" else "今日课程"`

## 五、接入「今日课程」页（两处逻辑必须一致）
- [ ] `screens/miuix/TodayScreen.kt`（当前逻辑在 `:59-63`）
- [ ] `screens/material3/Material3TodayScreen.kt`（当前逻辑在 `:59-63`）
- [ ] 用 Resolver 结果替换现有 `weekCourses` / `todayCourses`
- [ ] 标题动态显示「今日课程」/「明日课程」
- [ ] 假期显示「假期中」，未登录仍显示「请登录使用」

## 六、自动刷新（关键！跨过时间点不会自动重组）
- [ ] `LaunchedEffect(enabled, hour, minute) { while (true) { state = resolve(now); delay(30_000) } }`
- [ ] 标题、列表、假期状态全部由这个 state 驱动
- [ ] 确认 iOS 从后台回前台后能自校正（30s 轮询可兜底）

## 七、边界情况（务必覆盖）
- [ ] 跨周：周六晚切到周日属于下一周，必须用 `targetDate` 重算周次，不能用 `viewModel.currentWeek`（`App.kt:87` 跨天/跨周不更新）
- [ ] 假期：`targetWeek <= 0` 或 `> totalWeek`
- [ ] `AppConfig.localDate`（`AppConfig.kt:21`）是初始化时算死的，禁止使用，一律用实时 `Clock.System.now()`
- [ ] `startDate == null` 时降级到 `currentWeek`
- [ ] 未登录（`username/password` 为空）

## 八、验证
- [ ] 把切换时间设成「1 分钟后」，观察是否自动切换
- [ ] 周六晚上切换，确认取到的是下一周（周日）的课
- [ ] 假期（第 0 周 / 超过总周数）显示「假期中」
- [ ] `startDate` 为空、未登录不崩溃
- [ ] 两种 UI 风格（Miuix / Material 3）表现一致
- [ ] 编译验证 `import top.yukonga.miuix.kmp.basic.NumberPicker` 可用

## 九、动手顺序
1. [ ] `AppConfig` 字段 + 持久化
2. [ ] Miuix 设置页（滚轮弹窗）
3. [ ] Material 3 设置页
4. [ ] `TodayScheduleResolver` + 两个 TodayScreen
5. [ ] 自测上述边界

# 更改横竖屏逻辑：强制竖屏，取消横屏
