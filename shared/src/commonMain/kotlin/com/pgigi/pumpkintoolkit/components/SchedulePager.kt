package com.pgigi.pumpkintoolkit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.models.ScheduleTime
import com.pgigi.pumpkintoolkit.utils.WeekCalculator
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Location
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import kotlin.time.Clock

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

val colorMap = mutableMapOf<String,Int>()
val colorIndex = mutableIntStateOf(0)

object CourseDetailSheet{
    var show = mutableStateOf(false)
    val name = mutableStateOf("")
    val classroom = mutableStateOf("")
    val teacher = mutableStateOf("")
    val weeks = mutableStateOf("")

    fun show(course: Course){
        name.value = course.name
        classroom.value = course.classroom
        teacher.value = course.teacher
        weeks.value = "第"
        for (week in course.weeks){
            if(week.size == 1){
                weeks.value += week[0]
            }else if (week.size == 2){
                if (week[0]==week[1]){
                    weeks.value += week[0]
                }else{
                    weeks.value += week[0].toString() + "-" + week[1]
                }
            }else{
                continue
            }
            weeks.value += "、"
        }
        weeks.value = weeks.value.dropLast(1)+"周"
        show.value = true
    }
}

@Composable
fun CourseCell(course: Course, modifier: Modifier = Modifier){
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

    Column(modifier = modifier
        .clip(RoundedCornerShape(4.dp))
        .background(baseColor.copy(alpha = 0.75f))
        .fillMaxWidth()
        .clickable(true) {
            CourseDetailSheet.show(course)
        }
    ) {

        Text(text = course.name,
            modifier = Modifier.padding(3.dp),
            maxLines = AppConfig.courseNameLine,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp)
        Text(text = "@$classroom",
            modifier = Modifier.padding(3.dp),
            maxLines = AppConfig.courseRoomLine,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
            fontSize = 10.sp)
        Text(text = course.teacher,
            modifier = Modifier.padding(3.dp),
            maxLines = AppConfig.courseTeacherLine,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
            fontSize = 10.sp)
    }

}

@Composable
fun CourseRow(dayCourses: List<Course>, cellHeight: Dp = 70.dp, modifier: Modifier = Modifier) {

    Box(modifier = modifier.height(cellHeight * 10)) {
        dayCourses.forEach { course ->
            CourseCell(course, Modifier
                .padding(1.dp)
                .padding(top = cellHeight * (course.lessonOfDay - 1) + 1.dp)
                .height(cellHeight * course.duration - 2.dp)
            )
        }
    }
}

@Composable
fun TimeColumn(timeList: List<ScheduleTime>, count: Int = timeList.size, cellHeight: Dp = 70.dp) {

    Column(
        Modifier
            .width(30.dp)
            .height(cellHeight * 10)
    ) {
        repeat(maxOf(timeList.size, count)) { i ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = (i + 1).toString(),
                    fontSize = 12.sp
                )
                if (i < timeList.size) {
                    Text(
                        text = "${timeList[i].start}\n${timeList[i].end}",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleTable(courses: List<Course>, timeList: List<ScheduleTime>, timeListCount: Int = timeList.size, modifier: Modifier = Modifier, cellHeight: Dp = 70.dp, firstDay: LocalDate? = null){
    var dayList by mutableStateOf(listOf("日","一","二","三","四","五","六"))

    val dayCourses by produceState(initialValue = Array(7) { emptyList() }, courses) {
        value = Array(7) { day ->
            mergeCoursesForDisplay(courses.filter { it.dayOfWeek == day })
        }
    }

    // 缓存日期计算
    val dates = remember(firstDay) {
        firstDay?.let { start ->
            List(7) { offset -> start.plus(offset, DateTimeUnit.DAY) }
        }
    }

    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    Column(modifier.fillMaxWidth()) {
        // 表头使用LazyRow如果课程很多
        Row(Modifier
            .fillMaxWidth()
            .height(40.dp)) {
            // 月份显示
            Box(Modifier
                .width(30.dp)
                .fillMaxHeight()) {
                Text(
                    text = if(firstDay == null) "" else "${firstDay.month.number}\n月",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }

            dayList.forEachIndexed { index, day ->
                val date = dates?.get(index)
                val isToday = remember(date, today) {
                    if(date!=null) date == today else false
                }
                Column(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = day,
                        color = if (isToday) MiuixTheme.colorScheme.primary else Color.Unspecified,
                        fontSize = 12.sp
                    )
                    if (date != null) {
                        Text(
                            text = date.day.toString(),
                            color = if (isToday) MiuixTheme.colorScheme.primary else Color.Unspecified,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 课程表主体
        Row(Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())) {
            // 时间列
            TimeColumn(timeList, timeListCount, cellHeight)

            // 课程列
            dayCourses.forEach { dayCourseList ->
                CourseRow(
                    dayCourses = dayCourseList,
                    cellHeight = cellHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SchedulePager(modifier: Modifier = Modifier,
                  courseListByWeek: List<List<Course>>,
                  timeList: List<ScheduleTime>,
                  timeListCount: Int = 10,
                  cellHeight: Dp = 70.dp,
                  pagerState: PagerState,
                  startDate: LocalDate? = null) {

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { page ->
        if (startDate != null) {
            val weekCalculator = WeekCalculator(startDate)
            ScheduleTable(
                courses = courseListByWeek.getOrElse(page) { listOf() },
                timeList = timeList,
                timeListCount = timeListCount,
                cellHeight = cellHeight,
                firstDay = weekCalculator.getWeekFirstDay(page + 1)
            )
        }else {
            ScheduleTable(
                courses = courseListByWeek.getOrElse(page) { listOf() },
                timeList = timeList,
                cellHeight = cellHeight,
                timeListCount = timeListCount
            )
        }
    }


    WindowBottomSheet(
        show = CourseDetailSheet.show.value,
        title = "课程详细",
        onDismissRequest = { CourseDetailSheet.show.value = false }
    ) {
        BasicComponent(
            title = CourseDetailSheet.name.value,
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Info,
                    contentDescription = "课程: "+ CourseDetailSheet.name.value
                )
            }
        )
        if(CourseDetailSheet.classroom.value.isNotEmpty()){
            BasicComponent(
                title = CourseDetailSheet.classroom.value,
                startAction = {
                    Icon(
                        imageVector = MiuixIcons.Location,
                        contentDescription = "教室: " + CourseDetailSheet.classroom.value
                    )
                }
            )
        }
        if(CourseDetailSheet.teacher.value.isNotEmpty()){
            BasicComponent(
                title = CourseDetailSheet.teacher.value,
                startAction = {
                    Icon(
                        imageVector = MiuixIcons.Contacts,
                        contentDescription = "教师: " + CourseDetailSheet.teacher.value
                    )
                }
            )
        }
        BasicComponent(
            title = CourseDetailSheet.weeks.value,
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Months,
                    contentDescription = "周次: "+ CourseDetailSheet.weeks.value
                )
            }
        )
        BasicComponent()
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

private fun mergeCoursesForDisplay(courses: List<Course>): List<Course> {
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

    // 保留不在 1-8 节范围内的课程原样展示
    result.addAll(sortedCourses.filter { course ->
        val start = course.lessonOfDay
        val end = course.lessonOfDay + course.duration - 1
        end < 1 || start > 8
    })

    return result.sortedBy { it.lessonOfDay }
}
