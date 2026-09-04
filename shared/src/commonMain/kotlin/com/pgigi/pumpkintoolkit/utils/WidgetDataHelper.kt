package com.pgigi.pumpkintoolkit.utils

import com.pgigi.pumpkintoolkit.constants.TimeList
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.models.ScheduleTime
import com.pgigi.pumpkintoolkit.models.WidgetCourseItem
import com.pgigi.pumpkintoolkit.models.WidgetData
import com.pgigi.pumpkintoolkit.models.WidgetScheduleTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

object WidgetDataHelper {

    val DAY_OF_WEEK_TEXT = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    fun buildWidgetData(
        courses: List<Course>,
        startDate: LocalDate?,
        totalWeek: Int,
        timeList: List<ScheduleTime>
    ): WidgetData {
        val widgetCourses = courses.map { course ->
            WidgetCourseItem(
                name = course.name,
                classroom = course.classroom,
                teacher = course.teacher,
                dayOfWeek = course.dayOfWeek,
                lessonOfDay = course.lessonOfDay,
                duration = course.duration,
                weeks = course.weeks.map { listOf(it[0], it[1]) }
            )
        }
        val widgetTimeList = timeList.map { WidgetScheduleTime(it.start, it.end) }
        return WidgetData(
            startDate = startDate?.toString(),
            totalWeek = totalWeek,
            timeList = widgetTimeList,
            courses = widgetCourses,
            updateTime = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun buildWidgetData(
        courses: List<Course>,
        startDate: LocalDate?,
        totalWeek: Int,
        timeSeason: Int
    ): WidgetData {
        val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val timeList = when (timeSeason) {
            1 -> TimeList.summerAutumnTime
            2 -> TimeList.winterSpringTime
            else -> if (localDate.month.number in 5..<10) TimeList.summerAutumnTime else TimeList.winterSpringTime
        }
        return buildWidgetData(courses, startDate, totalWeek, timeList)
    }

    fun getWeekNumber(startDateStr: String?, date: LocalDate): Int {
        if (startDateStr == null) return 0
        return try {
            val startDate = LocalDate.parse(startDateStr)
            WeekCalculator(startDate, 1).getWeekNumber(date).toInt()
        } catch (_: Exception) { 0 }
    }

    fun isHoliday(weekNumber: Int, totalWeek: Int): Boolean {
        return weekNumber <= 0 || (totalWeek > 0 && weekNumber > totalWeek)
    }

    fun getDayOfWeek(date: LocalDate): Int {
        return (date.dayOfWeek.ordinal + 1) % 7
    }

    fun getDayOfWeekText(dayOfWeek: Int): String {
        return DAY_OF_WEEK_TEXT.getOrElse(dayOfWeek) { "" }
    }

    fun currentTimeStr(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
    }

    fun getTodayDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun getRemainingCourses(
        data: WidgetData,
        date: LocalDate,
        currentTime: String
    ): List<DisplayCourse> {
        val weekNumber = getWeekNumber(data.startDate, date)
        if (isHoliday(weekNumber, data.totalWeek)) return emptyList()

        val dayOfWeek = getDayOfWeek(date)
        return data.courses
            .filter { course ->
                course.dayOfWeek == dayOfWeek &&
                course.weeks.any { it[0] <= weekNumber && it[1] >= weekNumber }
            }
            .sortedBy { it.lessonOfDay }
            .filterNot { course ->
                val endIndex = (course.lessonOfDay - 1 + course.duration - 1)
                    .coerceIn(0, data.timeList.lastIndex)
                data.timeList[endIndex].end < currentTime
            }
            .map { course -> toDisplayCourse(course, data.timeList, isTomorrow = false) }
    }

    fun getTomorrowCourses(data: WidgetData, todayDate: LocalDate): List<DisplayCourse> {
        val tomorrow = todayDate.plus(1, DateTimeUnit.DAY)
        val weekNumber = getWeekNumber(data.startDate, tomorrow)
        if (isHoliday(weekNumber, data.totalWeek)) return emptyList()

        val dayOfWeek = getDayOfWeek(tomorrow)
        return data.courses
            .filter { course ->
                course.dayOfWeek == dayOfWeek &&
                course.weeks.any { it[0] <= weekNumber && it[1] >= weekNumber }
            }
            .sortedBy { it.lessonOfDay }
            .map { course -> toDisplayCourse(course, data.timeList, isTomorrow = true) }
    }

    private fun toDisplayCourse(
        course: WidgetCourseItem,
        timeList: List<WidgetScheduleTime>,
        isTomorrow: Boolean
    ): DisplayCourse {
        val startIndex = (course.lessonOfDay - 1).coerceIn(0, timeList.lastIndex)
        val endIndex = (startIndex + course.duration - 1).coerceIn(0, timeList.lastIndex)
        val classroom = course.classroom
            .replace("【红湘校区】", "")
            .replace("【雨母校区】", "")
        return DisplayCourse(
            name = course.name,
            classroom = classroom,
            teacher = course.teacher,
            startTime = timeList[startIndex].start,
            endTime = timeList[endIndex].end,
            isTomorrow = isTomorrow
        )
    }
}

data class DisplayCourse(
    val name: String,
    val classroom: String,
    val teacher: String,
    val startTime: String,
    val endTime: String,
    val isTomorrow: Boolean
)
