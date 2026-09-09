package com.pgigi.pumpkintoolkit.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
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
    val cardBackground: Color,
    val onCard: Color,
    val tomorrowCardBackground: Color,
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
        cardBackground = if (dark) Color(0xFF2A2A2A) else Color.White,
        onCard = if (dark) Color(0xFFE0E0E0) else Color(0xFF1A1A1A),
        tomorrowCardBackground = if (dark) Color(0xFF2D2520) else Color(0xFFFFF5F0),
        onTomorrowCard = if (dark) Color(0xFFFFB08A) else Color(0xFFCC4A1F),
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

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .cornerRadius(16.dp)
            .padding(all = 12.dp)
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
    val firstCourse = state.todayCourses.firstOrNull() ?: state.tomorrowCourses.firstOrNull()

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(state.weekNumber, state.dayOfWeekText, state.isHoliday, colors)
        if (firstCourse == null) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "今日无课",
                    style = TextStyle(
                        color = FixedColorProvider(colors.secondary),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            SmallCourseCard(firstCourse, colors)
            Spacer(modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun MediumWidgetView(state: WidgetState, colors: WidgetColors, width: Dp) {
    val maxToday = when {
        width < 200.dp -> 1
        width < 280.dp -> 2
        else -> 3
    }
    val maxTomorrow = if (width < 200.dp) 1 else 2

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(state.weekNumber, state.dayOfWeekText, state.isHoliday, colors)
        if (state.todayCourses.isEmpty() && state.tomorrowCourses.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "今日无课",
                    style = TextStyle(
                        color = FixedColorProvider(colors.secondary),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            state.todayCourses.take(maxToday).forEach { course ->
                CourseCard(course, colors, isTomorrow = false)
            }
            if (state.todayCourses.size < maxToday && state.tomorrowCourses.isNotEmpty()) {
                TomorrowSeparator(colors)
                state.tomorrowCourses.take(maxTomorrow).forEach { course ->
                    CourseCard(course, colors, isTomorrow = true)
                }
            }
        }
    }
}

@Composable
private fun LargeWidgetView(state: WidgetState, colors: WidgetColors, height: Dp) {
    val maxToday = when {
        height < 200.dp -> 3
        height < 280.dp -> 5
        else -> 6
    }
    val maxTomorrow = when {
        height < 200.dp -> 2
        height < 280.dp -> 3
        else -> 4
    }

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
                    "今日无课",
                    style = TextStyle(
                        color = FixedColorProvider(colors.secondary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            state.todayCourses.take(maxToday).forEach { course ->
                CourseCard(course, colors, isTomorrow = false)
            }
            if (state.tomorrowCourses.isNotEmpty()) {
                TomorrowSeparator(colors)
                state.tomorrowCourses.take(maxTomorrow).forEach { course ->
                    CourseCard(course, colors, isTomorrow = true)
                }
            }
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
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

@Composable
private fun SmallCourseCard(
    course: DisplayCourse,
    colors: WidgetColors
) {
    val isTomorrow = course.isTomorrow
    val bg = if (isTomorrow) colors.tomorrowCardBackground else colors.cardBackground
    val onColor = if (isTomorrow) colors.onTomorrowCard else colors.onCard
    val accentColor = if (isTomorrow) colors.onTomorrowCard else colors.secondary

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(bg)
            .cornerRadius(10.dp)
            .padding(all = 10.dp)
    ) {
        Column {
            Text(
                course.name,
                style = TextStyle(
                    color = FixedColorProvider(onColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.height(3.dp))
            Text(
                "${course.startTime}-${course.endTime}",
                style = TextStyle(
                    color = FixedColorProvider(accentColor),
                    fontSize = 11.sp
                )
            )
            if (course.classroom.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(3.dp))
                Text(
                    course.classroom,
                    style = TextStyle(
                        color = FixedColorProvider(accentColor),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: DisplayCourse,
    colors: WidgetColors,
    isTomorrow: Boolean
) {
    val bg = if (isTomorrow) colors.tomorrowCardBackground else colors.cardBackground
    val onColor = if (isTomorrow) colors.onTomorrowCard else colors.onCard
    val accentColor = if (isTomorrow) colors.onTomorrowCard else colors.secondary

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(bg)
            .cornerRadius(10.dp)
            .padding(all = 10.dp)
    ) {
        Column {
            Text(
                course.name,
                style = TextStyle(
                    color = FixedColorProvider(onColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${course.startTime}-${course.endTime}",
                    style = TextStyle(
                        color = FixedColorProvider(accentColor),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                if (course.classroom.isNotEmpty()) {
                    Text(
                        course.classroom,
                        style = TextStyle(
                            color = FixedColorProvider(accentColor),
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (course.teacher.isNotEmpty()) {
                    Text(
                        course.teacher,
                        style = TextStyle(
                            color = FixedColorProvider(accentColor),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
