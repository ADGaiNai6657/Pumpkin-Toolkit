package com.pgigi.pumpkintoolkit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
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
