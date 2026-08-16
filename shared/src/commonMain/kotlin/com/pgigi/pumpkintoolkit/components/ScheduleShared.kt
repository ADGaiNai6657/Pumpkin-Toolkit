package com.pgigi.pumpkintoolkit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.models.Course

val ScheduleCellColors = arrayOf(
    Color(0xffff1644),
    Color(0xff2b78ff),
    Color(0xffa375ff),
    Color(0xffff9000),
    Color(0xff1ee8b5),
    Color(0xffff3d01),
    Color(0xff2097f4),
    Color(0xff005daf),
    Color(0xfffa6278)
)

val colorMap = mutableMapOf<String, Int>()
val colorIndex = mutableIntStateOf(0)

object CourseDetailSheet {
    var show = mutableStateOf(false)
    val name = mutableStateOf("")
    val classroom = mutableStateOf("")
    val teacher = mutableStateOf("")
    val weeks = mutableStateOf("")

    fun show(course: Course) {
        name.value = course.name
        classroom.value = course.classroom
        teacher.value = course.teacher
        weeks.value = "第"
        for (week in course.weeks) {
            if (week.size == 1) {
                weeks.value += week[0]
            } else if (week.size == 2) {
                if (week[0] == week[1]) {
                    weeks.value += week[0]
                } else {
                    weeks.value += week[0].toString() + "-" + week[1]
                }
            } else {
                continue
            }
            weeks.value += "、"
        }
        weeks.value = weeks.value.dropLast(1) + "周"
        show.value = true
    }
}

object ConflictSheet {
    var show = mutableStateOf(false)
    val courses = mutableStateListOf<Course>()

    fun show(conflictCourses: List<Course>) {
        courses.clear()
        courses.addAll(conflictCourses)
        show.value = true
    }
}

@Composable
fun CourseCell(course: Course, conflictCourses: List<Course> = emptyList(), modifier: Modifier = Modifier) {
    val hasConflict = conflictCourses.size > 1

    val colorIndex = remember(course.name) {
        colorMap.getOrPut(course.name) { colorIndex.value++ }
    }

    val baseColor = ScheduleCellColors[colorIndex % ScheduleCellColors.size]

    val classroom = remember(course.name) {
        course.classroom
            .replace("【雨母校区】", "")
            .replace("【红湘校区】", "")
    }

    val textColor = Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(baseColor.copy(alpha = 0.75f))
            .fillMaxWidth()
            .clickable(true) {
                if (hasConflict) {
                    ConflictSheet.show(conflictCourses)
                } else {
                    CourseDetailSheet.show(course)
                }
            }
    ) {
        Column {
            BasicText(
                text = course.name,
                modifier = Modifier.padding(3.dp),
                style = TextStyle(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                ),
                maxLines = AppConfig.courseNameLine,
                overflow = TextOverflow.Ellipsis
            )
            BasicText(
                text = "@$classroom",
                modifier = Modifier.padding(3.dp),
                style = TextStyle(
                    color = textColor,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                ),
                maxLines = AppConfig.courseRoomLine,
                overflow = TextOverflow.Ellipsis
            )
            BasicText(
                text = course.teacher,
                modifier = Modifier.padding(3.dp),
                style = TextStyle(
                    color = textColor,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                ),
                maxLines = AppConfig.courseTeacherLine,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (hasConflict) {
            Canvas(modifier = Modifier.align(Alignment.BottomEnd).size(20.dp)) {
                val path = Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color(0xFFFF0000))
            }
        }
    }
}

@Composable
fun CourseRow(dayCourses: List<Course>, cellHeight: Dp = 70.dp, modifier: Modifier = Modifier) {
    val courseGroups = remember(dayCourses) {
        groupConflictingCourses(dayCourses)
    }

    Box(modifier = modifier.height(cellHeight * 10)) {
        courseGroups.forEach { group ->
            val course = group.first()
            CourseCell(
                course = course,
                conflictCourses = group,
                modifier = Modifier
                    .padding(1.dp)
                    .padding(top = cellHeight * (course.lessonOfDay - 1) + 1.dp)
                    .height(cellHeight * course.duration - 2.dp)
            )
        }
    }
}

private fun Array<IntArray>.weeksEquals(other: Array<IntArray>): Boolean {
    if (size != other.size) return false
    return indices.all { idx -> this[idx].contentEquals(other[idx]) }
}

private fun canMergeCourse(a: Course, b: Course): Boolean {
    return a.name == b.name &&
            a.classroom == b.classroom &&
            a.teacher == b.teacher &&
            a.weeks.weeksEquals(b.weeks)
}

internal fun mergeCoursesForDisplay(courses: List<Course>): List<Course> {
    val sortedCourses = courses.sortedBy { it.lessonOfDay }
    if (sortedCourses.isEmpty()) return sortedCourses

    val result = mutableListOf<Course>()
    val ranges = listOf(1..4, 5..8)

    ranges.forEach { range ->
        val blockCourses = sortedCourses.filter { course ->
            val start = course.lessonOfDay
            val end = course.lessonOfDay + course.duration - 1
            start in range && end in range
        }
        if (blockCourses.isEmpty()) return@forEach

        var current = blockCourses.first().copy()
        for (next in blockCourses.drop(1)) {
            val currentEnd = current.lessonOfDay + current.duration - 1
            val isAdjacent = next.lessonOfDay == currentEnd + 1
            if (isAdjacent && canMergeCourse(current, next)) {
                current.duration += next.duration
            } else {
                result.add(current)
                current = next.copy()
            }
        }
        result.add(current)
    }

    result.addAll(sortedCourses.filter { course ->
        val start = course.lessonOfDay
        val end = course.lessonOfDay + course.duration - 1
        !(start in 1..4 && end in 1..4) && !(start in 5..8 && end in 5..8)
    })

    return result.sortedBy { it.lessonOfDay }
}

private fun groupConflictingCourses(courses: List<Course>): List<List<Course>> {
    val sorted = courses.sortedBy { it.lessonOfDay }
    if (sorted.isEmpty()) return emptyList()

    val groups = mutableListOf<MutableList<Course>>()
    var currentGroup = mutableListOf(sorted.first())
    var currentMaxEnd = sorted.first().let { it.lessonOfDay + it.duration - 1 }

    for (course in sorted.drop(1)) {
        if (course.lessonOfDay <= currentMaxEnd) {
            currentGroup.add(course)
            currentMaxEnd = maxOf(currentMaxEnd, course.lessonOfDay + course.duration - 1)
        } else {
            groups.add(currentGroup)
            currentGroup = mutableListOf(course)
            currentMaxEnd = course.lessonOfDay + course.duration - 1
        }
    }
    groups.add(currentGroup)

    return groups
}
