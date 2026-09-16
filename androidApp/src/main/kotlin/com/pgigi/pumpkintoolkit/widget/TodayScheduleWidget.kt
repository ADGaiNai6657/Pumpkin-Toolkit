package com.pgigi.pumpkintoolkit.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.FixedColorProvider
import com.pgigi.pumpkintoolkit.MainActivity
import com.pgigi.pumpkintoolkit.models.WidgetData
import com.pgigi.pumpkintoolkit.utils.DisplayCourse
import com.pgigi.pumpkintoolkit.utils.JsonUtil
import com.pgigi.pumpkintoolkit.utils.WidgetDataHelper
import java.io.File

class TodayScheduleWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp),
            DpSize(180.dp, 110.dp),
            DpSize(250.dp, 110.dp),
            DpSize(320.dp, 110.dp),
            DpSize(180.dp, 180.dp),
            DpSize(250.dp, 250.dp),
            DpSize(320.dp, 250.dp),
            DpSize(320.dp, 320.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = readWidgetData(context)
        provideContent {
            WidgetContent(data)
        }
    }

    private fun readWidgetData(context: Context): WidgetData? {
        return try {
            val file = File(context.filesDir, "widget_data.json")
            if (file.exists()) {
                val json = file.readText()
                JsonUtil.parseJson(json, WidgetData.serializer())
            } else null
        } catch (_: Exception) { null }
    }
}

private data class WidgetColors(
    val background: Color,
    val onBackground: Color,
    val onCard: Color,
    val onTomorrowCard: Color,
    val secondary: Color
)

@Composable
private fun isDarkTheme(): Boolean {
    val context = LocalContext.current
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
}

@Composable
private fun widgetColors(): WidgetColors {
    val dark = isDarkTheme()
    return WidgetColors(
        background = if (dark) Color(0xFF1B1B1B) else Color(0xFFF7F7F7),
        onBackground = if (dark) Color(0xFFE0E0E0) else Color(0xFF1A1A1A),
        // 今日课程使用默认颜色
        onCard = if (dark) Color(0xFFE0E0E0) else Color(0xFF1A1A1A),
        // 明日课程使用灰色
        onTomorrowCard = if (dark) Color(0xFF888888) else Color(0xFF666666),
        secondary = if (dark) Color(0xFF888888) else Color(0xFF666666)
    )
}

private data class WidgetState(
    val data: WidgetData?,
    val weekNumber: Int,
    val totalWeek: Int,
    val isHoliday: Boolean,
    val dayOfWeekText: String,
    val todayCourses: List<DisplayCourse>,
    val tomorrowCourses: List<DisplayCourse>
)

@Composable
private fun rememberWidgetState(data: WidgetData?): WidgetState {
    val today = WidgetDataHelper.getTodayDate()
    val currentTime = WidgetDataHelper.currentTimeStr()
    val weekNumber = data?.let { WidgetDataHelper.getWeekNumber(it.startDate, today) } ?: 0
    val totalWeek = data?.totalWeek ?: 0
    val isHoliday = data != null && WidgetDataHelper.isHoliday(weekNumber, totalWeek)
    val dayOfWeek = WidgetDataHelper.getDayOfWeek(today)
    val dayOfWeekText = WidgetDataHelper.getDayOfWeekText(dayOfWeek)

    val todayCourses = if (data != null && !isHoliday)
        WidgetDataHelper.getRemainingCourses(data, today, currentTime) else emptyList()
    val tomorrowCourses = if (data != null && !isHoliday)
        WidgetDataHelper.getTomorrowCourses(data, today) else emptyList()

    return WidgetState(data, weekNumber, totalWeek, isHoliday, dayOfWeekText, todayCourses, tomorrowCourses)
}

@Composable
private fun WidgetContent(data: WidgetData?) {
    val colors = widgetColors()
    val state = rememberWidgetState(data)
    val size = LocalSize.current
    val context = LocalContext.current
    val intent = remember { Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK } }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .cornerRadius(16.dp)
            .padding(all = 12.dp)
            .clickable(actionStartActivity(intent))
    ) {
        if (state.data == null) {
            NoDataView(colors)
        } else if (state.isHoliday) {
            HolidayView(state.dayOfWeekText, colors)
        } else {
            when {
                size.width < 140.dp && size.height < 140.dp -> SmallWidgetView(state, colors)
                size.height < 150.dp -> MediumWidgetView(state, colors, size.width)
                else -> LargeWidgetView(state, colors, size.height)
            }
        }
    }
}

@Composable
private fun NoDataView(colors: WidgetColors) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "请打开应用刷新",
            style = TextStyle(
                color = FixedColorProvider(colors.secondary),
                fontSize = 14.sp
            )
        )
    }
}

@Composable
private fun HolidayView(dayOfWeekText: String, colors: WidgetColors) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(0, dayOfWeekText, true, colors)
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "假期中",
                style = TextStyle(
                    color = FixedColorProvider(colors.secondary),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun SmallWidgetView(state: WidgetState, colors: WidgetColors) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(state.weekNumber, state.dayOfWeekText, state.isHoliday, colors)
        if (state.todayCourses.isEmpty() && state.tomorrowCourses.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "今日无课了",
                    style = TextStyle(
                        color = FixedColorProvider(colors.secondary),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                // 今日课程
                state.todayCourses.forEachIndexed { index, course ->
                    item { CourseItem(course, colors, isTomorrow = false, isSmallWidget = true) }
                    // 添加分割线（除了最后一个）
                    // if (index < state.todayCourses.size - 1) {
                        // item { CourseDivider(colors) }
                    // }
                }
                // 明日课程
                if (state.tomorrowCourses.isNotEmpty()) {
                    item { TomorrowSeparator(colors) }
                    state.tomorrowCourses.forEachIndexed { index, course ->
                        item { CourseItem(course, colors, isTomorrow = true, isSmallWidget = true) }
                        // 添加分割线（除了最后一个）
                        // if (index < state.tomorrowCourses.size - 1) {
                            // item { CourseDivider(colors) }
                        // }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediumWidgetView(state: WidgetState, colors: WidgetColors, width: Dp) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(state.weekNumber, state.dayOfWeekText, state.isHoliday, colors)
        if (state.todayCourses.isEmpty() && state.tomorrowCourses.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "今日无课了",
                    style = TextStyle(
                        color = FixedColorProvider(colors.secondary),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                // 今日课程
                state.todayCourses.forEachIndexed { index, course ->
                    item { CourseItem(course, colors, isTomorrow = false, isSmallWidget = false) }
                    // 添加分割线（除了最后一个）
                    // if (index < state.todayCourses.size - 1) {
                        // item { CourseDivider(colors) }
                    // }
                }
                // 明日课程
                if (state.tomorrowCourses.isNotEmpty()) {
                    item { TomorrowSeparator(colors) }
                    state.tomorrowCourses.forEachIndexed { index, course ->
                        item { CourseItem(course, colors, isTomorrow = true, isSmallWidget = false) }
                        // 添加分割线（除了最后一个）
                        // if (index < state.tomorrowCourses.size - 1) {
                            // item { CourseDivider(colors) }
                        // }
                    }
                }
            }
        }
    }
}

@Composable
private fun LargeWidgetView(state: WidgetState, colors: WidgetColors, height: Dp) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(state.weekNumber, state.dayOfWeekText, state.isHoliday, colors)
        if (state.todayCourses.isEmpty() && state.tomorrowCourses.isEmpty()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "今日无课了",
                    style = TextStyle(
                        color = FixedColorProvider(colors.secondary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                // 今日课程
                state.todayCourses.forEachIndexed { index, course ->
                    item { CourseItem(course, colors, isTomorrow = false, isSmallWidget = false) }
                    // 添加分割线（除了最后一个）
                    // if (index < state.todayCourses.size - 1) {
                        // item { CourseDivider(colors) }
                    // }
                }
                // 明日课程
                if (state.tomorrowCourses.isNotEmpty()) {
                    item { TomorrowSeparator(colors) }
                    state.tomorrowCourses.forEachIndexed { index, course ->
                        item { CourseItem(course, colors, isTomorrow = true, isSmallWidget = false) }
                        // 添加分割线（除了最后一个）
                        // if (index < state.tomorrowCourses.size - 1) {
                            // item { CourseDivider(colors) }
                        // }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    weekNumber: Int,
    dayOfWeekText: String,
    isHoliday: Boolean,
    colors: WidgetColors
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isHoliday && weekNumber > 0) {
            Text(
                "第${weekNumber}周",
                style = TextStyle(
                    color = FixedColorProvider(colors.onBackground),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            dayOfWeekText,
            style = TextStyle(
                color = FixedColorProvider(colors.onBackground),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun CourseDivider(colors: WidgetColors) {
    // 比明日课程分隔线颜色浅的灰色
    val dividerColor = if (isDarkTheme()) Color(0xFF444444) else Color(0xFFE0E0E0)
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(1.dp)
            .background(dividerColor)
    ) {
        // 空内容
    }
}

@Composable
private fun CourseItem(
    course: DisplayCourse,
    colors: WidgetColors,
    isTomorrow: Boolean,
    isSmallWidget: Boolean = false
) {
    val isCurrentCourse = course.isCurrentCourse
    // 当前课程使用橙黄色，今日课程使用默认颜色，明日课程使用灰色
    val onColor = when {
        isCurrentCourse -> Color(0xFFFF9800) // 橙黄色文字
        isTomorrow -> colors.onTomorrowCard
        else -> colors.onCard // 默认颜色
    }
    val accentColor = when {
        isCurrentCourse -> Color(0xFFFF9800) // 橙黄色文字
        isTomorrow -> colors.onTomorrowCard
        else -> colors.secondary // 默认颜色
    }

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(all = 4.dp)
    ) {
        if(isSmallWidget){
            // 课程名称
            Text(
                course.name,
                maxLines = 1,
                style = TextStyle(
                    color = FixedColorProvider(onColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            // 时间
            Text(
                "${course.startTime}-${course.endTime}",
                style = TextStyle(
                    color = FixedColorProvider(accentColor),
                    fontSize = 11.sp
                )
            )

            // 地点（如果存在）
            if (course.classroom.isNotEmpty()) {
                Text(
                    course.classroom,
                    style = TextStyle(
                        color = FixedColorProvider(accentColor),
                        fontSize = 11.sp
                    )
                )
            }
        }else{
            // 课程名称
            Text(
                course.name,
                maxLines = 1,
                style = TextStyle(
                    color = FixedColorProvider(onColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            val secondTextArray = mutableListOf("${course.startTime}-${course.endTime}")
            if(course.classroom.isNotEmpty()) secondTextArray.add(course.classroom)
            if(course.teacher.isNotEmpty()) secondTextArray.add(course.teacher)

            Text(
                secondTextArray.joinToString("・"),
                style = TextStyle(
                    color = FixedColorProvider(accentColor),
                    fontSize = 11.sp
                )
            )
        }
        

    }
}

@Composable
private fun TomorrowSeparator(colors: WidgetColors) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .height(1.dp)
                .background(colors.secondary)
                .defaultWeight()
        ) {}
        Text(
            "明日",
            modifier = GlanceModifier.padding(horizontal = 8.dp),
            style = TextStyle(
                color = FixedColorProvider(colors.secondary),
                fontSize = 11.sp
            )
        )
        Box(
            modifier = GlanceModifier
                .height(1.dp)
                .background(colors.secondary)
                .defaultWeight()
        ) {}
    }
}

