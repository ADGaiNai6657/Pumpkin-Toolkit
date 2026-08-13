package com.pgigi.pumpkintoolkit.components

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Clock


class NumberDatePickerState {
    var year by mutableIntStateOf(1970)
    var month by mutableIntStateOf(1)
    var day by mutableIntStateOf(1)

    constructor() {
        val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        year = localDate.year
        month = localDate.month.number
        day = localDate.day
    }

    constructor(localDate: LocalDate) {
        year = localDate.year
        month = localDate.month.number
        day = localDate.day
    }

    constructor(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month.coerceIn(1, 12)
        this.day = day.coerceIn(1, 31)
    }
}


@Composable
fun rememberNumberDatePickerState(): NumberDatePickerState {
    return remember {
        NumberDatePickerState()
    }
}

@Composable
fun rememberNumberDatePickerState(localDate: LocalDate): NumberDatePickerState {
    return remember {
        NumberDatePickerState(localDate)
    }
}

@Composable
fun rememberNumberDatePickerState(year: Int, month: Int, day: Int): NumberDatePickerState {
    return remember {
        NumberDatePickerState(year, month, day)
    }
}

@Composable
fun NumberDatePicker(
    modifier: Modifier = Modifier,
    numberDatePickerState: NumberDatePickerState,
    start: LocalDate = LocalDate(1970, 1, 1),
    end: LocalDate = LocalDate(2099, 12, 31)
) {
    // 确保月份合法
    val safeMonth = numberDatePickerState.month.coerceIn(1, 12)
    val yearMonth = remember(numberDatePickerState.year, safeMonth) {
        YearMonth(numberDatePickerState.year, safeMonth)
    }

    val days = remember(yearMonth) {
        1..yearMonth.lastDay.day
    }

    val months = 1..12
    val years = remember(start, end) {
        start.year..end.year
    }

    Row(modifier = modifier) {
        NumberPicker(
            modifier = Modifier.weight(1f),
            range = years,
            value = numberDatePickerState.year,
            label = {num -> "${num}年"},
            onValueChange = { numberDatePickerState.year = it },
            visibleItemCount = 3,
            textStyle = MiuixTheme.textStyles.main
        )
//        Text("年", modifier = Modifier.align(Alignment.CenterVertically))

        NumberPicker(
            modifier = Modifier.weight(1f),
            range = months,
            value = safeMonth,
            label = {num -> "${num}月"},
            onValueChange = {
                numberDatePickerState.month = it
                val yearMonth = YearMonth(numberDatePickerState.year, it)

                val days = 1..yearMonth.lastDay.day
                numberDatePickerState.day = numberDatePickerState.day.coerceIn(days)
            },
            visibleItemCount = 3,
            textStyle = MiuixTheme.textStyles.main
        )
//        Text("月", modifier = Modifier.align(Alignment.CenterVertically))

        NumberPicker(
            modifier = Modifier.weight(1f),
            range = days,
            value = numberDatePickerState.day.coerceIn(days),
            label = {num -> "${num}日"},
            onValueChange = { numberDatePickerState.day = it },
            visibleItemCount = 3,
            textStyle = MiuixTheme.textStyles.main
        )
//        Text("日", modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
@Preview
fun DatePickerPreview(){
    NumberDatePicker(numberDatePickerState = rememberNumberDatePickerState())
}