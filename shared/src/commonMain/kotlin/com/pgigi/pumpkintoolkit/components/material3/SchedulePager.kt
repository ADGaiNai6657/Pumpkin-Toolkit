package com.pgigi.pumpkintoolkit.components.material3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgigi.pumpkintoolkit.components.ConflictSheet
import com.pgigi.pumpkintoolkit.components.CourseDetailSheet
import com.pgigi.pumpkintoolkit.components.CourseRow
import com.pgigi.pumpkintoolkit.components.mergeCoursesForDisplay
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.models.ScheduleTime
import com.pgigi.pumpkintoolkit.utils.WeekCalculator
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Location
import top.yukonga.miuix.kmp.icon.extended.Months
import kotlin.time.Clock

@Composable
fun TimeColumn(timeList: List<ScheduleTime>, cellHeight: Dp = 70.dp) {
    Column(
        Modifier
            .width(30.dp)
            .height(cellHeight * timeList.size + 96.dp)
    ) {
        repeat(timeList.size) { i ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CompositionLocalProvider(
                    androidx.compose.material3.LocalTextStyle provides androidx.compose.material3.LocalTextStyle.current.merge(
                        TextStyle(
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
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
                            fontSize = 8.sp,
                            lineHeight = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleTable(
    courses: List<Course>,
    timeList: List<ScheduleTime>,
    modifier: Modifier = Modifier,
    cellHeight: Dp = 70.dp,
    firstDay: LocalDate? = null
) {
    var dayList by remember { mutableStateOf(listOf("日", "一", "二", "三", "四", "五", "六")) }

    val dayCourses by produceState(initialValue = Array(7) { emptyList() }, courses) {
        value = Array(7) { day ->
            mergeCoursesForDisplay(courses.filter { it.dayOfWeek == day })
        }
    }

    val dates = remember(firstDay) {
        firstDay?.let { start ->
            List(7) { offset -> start.plus(offset, DateTimeUnit.DAY) }
        }
    }

    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Column(
                Modifier
                    .width(30.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CompositionLocalProvider(
                    androidx.compose.material3.LocalTextStyle provides androidx.compose.material3.LocalTextStyle.current.merge(
                        TextStyle(
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                ) {
                    firstDay?.let {
                        Text(
                            text = firstDay.month.number.toString(),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "月",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            dayList.forEachIndexed { index, day ->
                val date = dates?.get(index)
                val isToday = remember(date, today) {
                    if (date != null) date == today else false
                }
                Column(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CompositionLocalProvider(
                        androidx.compose.material3.LocalTextStyle provides androidx.compose.material3.LocalTextStyle.current.merge(
                            TextStyle(
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    ) {
                        Text(
                            text = day,
                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Unspecified,
                            fontSize = 12.sp
                        )
                        if (date != null) {
                            Text(
                                text = date.day.toString(),
                                color = if (isToday) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            TimeColumn(timeList, cellHeight)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePager(
    modifier: Modifier = Modifier,
    courseListByWeek: List<List<Course>>,
    timeList: List<ScheduleTime>,
    cellHeight: Dp = 70.dp,
    pagerState: PagerState,
    startDate: LocalDate? = null
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { page ->
        if (startDate != null) {
            val weekCalculator = WeekCalculator(startDate)
            ScheduleTable(
                courses = courseListByWeek.getOrElse(page) { listOf() },
                timeList = timeList,
                cellHeight = cellHeight,
                firstDay = weekCalculator.getWeekFirstDay(page + 1)
            )
        } else {
            ScheduleTable(
                courses = courseListByWeek.getOrElse(page) { listOf() },
                timeList = timeList,
                cellHeight = cellHeight
            )
        }
    }

    if (CourseDetailSheet.show.value) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { CourseDetailSheet.show.value = false },
            sheetState = sheetState
        ) {
            Text(
                text = "课程详细",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            DetailRow(MiuixIcons.Info, "课程: " + CourseDetailSheet.name.value, CourseDetailSheet.name.value)
            if (CourseDetailSheet.classroom.value.isNotEmpty()) {
                DetailRow(MiuixIcons.Location, "教室: " + CourseDetailSheet.classroom.value, CourseDetailSheet.classroom.value)
            }
            if (CourseDetailSheet.teacher.value.isNotEmpty()) {
                DetailRow(MiuixIcons.Contacts, "教师: " + CourseDetailSheet.teacher.value, CourseDetailSheet.teacher.value)
            }
            DetailRow(MiuixIcons.Months, "周次: " + CourseDetailSheet.weeks.value, CourseDetailSheet.weeks.value)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (ConflictSheet.show.value) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { ConflictSheet.show.value = false },
            sheetState = sheetState
        ) {
            Text(
                text = "课程详细 (${ConflictSheet.courses.size}门)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            ConflictSheet.courses.forEach { course ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            ConflictSheet.show.value = false
                            CourseDetailSheet.show(course)
                        }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "@${course.classroom} ${course.teacher}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, contentDescription: String, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
