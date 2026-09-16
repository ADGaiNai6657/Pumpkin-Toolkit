package com.pgigi.pumpkintoolkit.utils

import com.pgigi.pumpkintoolkit.constants.TimeList
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.models.ScheduleTime
import com.pgigi.pumpkintoolkit.models.WidgetCourseItem
import com.pgigi.pumpkintoolkit.models.WidgetData
import com.pgigi.pumpkintoolkit.models.WidgetScheduleTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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

    fun isCurrentCourse(startTime: String, endTime: String, currentTime: String? = null): Boolean {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        // 使用传入的当前时间或系统当前时间
        val current = if (currentTime != null) {
            try {
                val parts = currentTime.split(":")
                if (parts.size == 2) {
                    LocalTime(parts[0].toInt(), parts[1].toInt())
                } else {
                    now.time
                }
            } catch (e: Exception) {
                now.time
            }
        } else {
            now.time
        }
        
        // 解析时间字符串
        return try {
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")
            
            if (startParts.size == 2 && endParts.size == 2) {
                val startHour = startParts[0].toInt()
                val startMinute = startParts[1].toInt()
                val endHour = endParts[0].toInt()
                val endMinute = endParts[1].toInt()
                
                val start = LocalTime(startHour, startMinute)
                val end = LocalTime(endHour, endMinute)
                
                // 当前时间在课程开始和结束时间之间（包括开始时间，不包括结束时间）
                current >= start && current < end
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
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
            .filter { course ->
                val endIndex = (course.lessonOfDay - 1 + course.duration - 1)
                    .coerceIn(0, data.timeList.lastIndex)
                // 课程结束时间严格大于当前时间才显示（结束时间点不显示）
                data.timeList[endIndex].end > currentTime
            }
            .map { course -> toDisplayCourse(course, data.timeList, isTomorrow = false, currentTime = currentTime) }
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
            .map { course -> toDisplayCourse(course, data.timeList, isTomorrow = true, currentTime = null) }
    }

    private fun toDisplayCourse(
        course: WidgetCourseItem,
        timeList: List<WidgetScheduleTime>,
        isTomorrow: Boolean,
        currentTime: String? = null
    ): DisplayCourse {
        val startIndex = (course.lessonOfDay - 1).coerceIn(0, timeList.lastIndex)
        val endIndex = (startIndex + course.duration - 1).coerceIn(0, timeList.lastIndex)
        val classroom = course.classroom
            .replace("【红湘校区】", "")
            .replace("【雨母校区】", "")
        val startTime = timeList[startIndex].start
        val endTime = timeList[endIndex].end
        // 明日课程的isCurrentCourse总是false
        val isCurrentCourse = if (isTomorrow) false else isCurrentCourse(startTime, endTime, currentTime)
        return DisplayCourse(
            name = course.name,
            classroom = classroom,
            teacher = course.teacher,
            startTime = startTime,
            endTime = endTime,
            isTomorrow = isTomorrow,
            isCurrentCourse = isCurrentCourse
        )
    }
}

data class DisplayCourse(
    val name: String,
    val classroom: String,
    val teacher: String,
    val startTime: String,
    val endTime: String,
    val isTomorrow: Boolean,
    val isCurrentCourse: Boolean // 是否是当前正在上的课
)
