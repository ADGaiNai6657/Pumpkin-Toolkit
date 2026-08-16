package com.pgigi.pumpkintoolkit.components.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgigi.pumpkintoolkit.components.NumberDatePickerState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import kotlin.time.Clock

@Composable
fun NumberDatePicker(
    modifier: Modifier = Modifier,
    numberDatePickerState: NumberDatePickerState,
    start: LocalDate = LocalDate(1970, 1, 1),
    end: LocalDate = LocalDate(2099, 12, 31)
) {
    val hapticFeedback = LocalHapticFeedback.current
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

    var displayYear by remember { mutableIntStateOf(numberDatePickerState.year) }
    var displayMonth by remember { mutableIntStateOf(numberDatePickerState.month) }

    val yearMonth = remember(displayYear, displayMonth) { YearMonth(displayYear, displayMonth) }
    val daysInMonth = yearMonth.lastDay.day

    val prevYearMonth = if (displayMonth == 1) YearMonth(displayYear - 1, 12) else YearMonth(displayYear, displayMonth - 1)
    val prevMonthDays = prevYearMonth.lastDay.day

    val firstDayOffset = remember(displayYear, displayMonth) {
        (LocalDate(displayYear, displayMonth, 1).dayOfWeek.ordinal + 1) % 7
    }

    val cells = remember(displayYear, displayMonth) {
        buildList<Pair<Int, Boolean>> {
            for (i in 0 until firstDayOffset) {
                add(Pair(prevMonthDays - firstDayOffset + 1 + i, false))
            }
            for (day in 1..daysInMonth) {
                add(Pair(day, true))
            }
            var nextDay = 1
            while (size < 42) {
                add(Pair(nextDay, false))
                nextDay++
            }
        }
    }

    Column(modifier = modifier.padding(horizontal = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (displayMonth == 1) { displayYear--; displayMonth = 12 }
                        else displayMonth--
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(MiuixIcons.ChevronBackward, "上个月", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            }

            Text(
                text = "${displayYear}年${displayMonth}月",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (displayMonth == 12) { displayYear++; displayMonth = 1 }
                        else displayMonth++
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(MiuixIcons.ChevronForward, "下个月", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { dow ->
                Text(
                    text = dow,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { (day, isCurrentMonth) ->
                    val date = if (isCurrentMonth) LocalDate(displayYear, displayMonth, day) else null
                    val isToday = date != null && date == today
                    val isSelected = isCurrentMonth &&
                        day == numberDatePickerState.day &&
                        displayMonth == numberDatePickerState.month &&
                        displayYear == numberDatePickerState.year
                    val isOutOfRange = date != null && (date < start || date > end)
                    val isSelectable = isCurrentMonth && !isOutOfRange

                    val cellModifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp)

                    val bgModifier = if (isSelected) {
                        Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                    } else if (isToday) {
                        Modifier.clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier
                    }

                    val clickModifier = if (isSelectable) {
                        Modifier.clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            numberDatePickerState.day = day
                            numberDatePickerState.month = displayMonth
                            numberDatePickerState.year = displayYear
                        }
                    } else {
                        Modifier
                    }

                    Box(
                        modifier = cellModifier.then(bgModifier).then(clickModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                    }
                }
            }
        }
    }
}
