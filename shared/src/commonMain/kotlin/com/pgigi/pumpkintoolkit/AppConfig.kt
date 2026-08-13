package com.pgigi.pumpkintoolkit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.constants.TimeList.summerAutumnTime
import com.pgigi.pumpkintoolkit.constants.TimeList.winterSpringTime
import com.pgigi.pumpkintoolkit.utils.KVaultUtils
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import kotlin.collections.set
import kotlin.time.Clock

object AppConfig {
    val kvault = KVaultUtils
    val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // UI
    var colorSchemeMode by mutableStateOf(ColorSchemeMode.System)

    // 课表
    var cellHeight by mutableIntStateOf(70)
    var courseNameLine by mutableIntStateOf(4)
    var courseRoomLine by mutableIntStateOf(2)
    var courseTeacherLine by mutableIntStateOf(2)
    var startDate by mutableStateOf<LocalDate?>(null)
    var timeSeason by mutableIntStateOf(0) // 0自动, 1夏秋, 2冬春
    var defaultTerm by mutableStateOf("")
    var totalWeekNum by mutableStateOf(0)

    var timeList by mutableStateOf(when(timeSeason){
        1 -> summerAutumnTime
        2 -> winterSpringTime
        else -> if(localDate.month.number in 5..<10) summerAutumnTime else winterSpringTime
    })

    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var totalWeek by mutableIntStateOf(0)
    var serverUrl by mutableStateOf("")



    object KEY {
        const val COLOR_MODE = "color_mode"
        const val CELL_HEIGHT = "cell_height"
        const val COURSE_NAME_LINE = "course_name_line"
        const val COURSE_ROOM_LINE = "course_room_line"
        const val COURSE_TEACHER_LINE = "course_teacher_line"
        const val TIME_SEASON = "time_season"
        const val START_DATE = "start_date"
        const val DEFAULT_TERM = "default_term"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val SERVER_URL = "server_url"
        const val TOTAL_WEEK = "total_week"
    }


    fun load(){

        colorSchemeMode = when(kvault.getString(KEY.COLOR_MODE)){
            ColorSchemeMode.Light.name -> ColorSchemeMode.Light
            ColorSchemeMode.Dark.name -> ColorSchemeMode.Dark
            else -> ColorSchemeMode.System
        }
        cellHeight = kvault.getInt(KEY.CELL_HEIGHT)?:70
        courseNameLine = kvault.getInt(KEY.COURSE_NAME_LINE)?:4
        courseRoomLine = kvault.getInt(KEY.COURSE_ROOM_LINE)?:2
        courseTeacherLine = kvault.getInt(KEY.COURSE_TEACHER_LINE)?:2

        timeSeason = kvault.getInt(KEY.TIME_SEASON)?:1

        timeList = when(timeSeason){
            1 -> summerAutumnTime
            2 -> winterSpringTime
            else -> if(localDate.month.number in 5..<10) summerAutumnTime else winterSpringTime
        }

        startDate = kvault.getString(KEY.START_DATE)?.let { LocalDate.parse(it) }
        defaultTerm = kvault.getString(KEY.DEFAULT_TERM)?:""
        username = kvault.getString(KEY.USERNAME)?:""
        password = kvault.getString(KEY.PASSWORD)?:""
        serverUrl = kvault.getString(KEY.SERVER_URL)?:"http://61.187.179.66:8924/"
        totalWeek = kvault.getInt(KEY.TOTAL_WEEK)?:0
    }

    fun save(){
        kvault.putString(KEY.COLOR_MODE,colorSchemeMode.name)
        kvault.putInt(KEY.CELL_HEIGHT,cellHeight)
        kvault.putInt(KEY.COURSE_NAME_LINE,courseNameLine)
        kvault.putInt(KEY.COURSE_ROOM_LINE,courseRoomLine)
        kvault.putInt(KEY.COURSE_TEACHER_LINE,courseTeacherLine)
        kvault.putString(KEY.USERNAME,username)
        kvault.putString(KEY.PASSWORD,password)
        kvault.putString(KEY.DEFAULT_TERM,defaultTerm)
        startDate?.toString()?.let { kvault.putString(KEY.START_DATE, it) }
        kvault.putInt(KEY.TIME_SEASON,timeSeason)
        kvault.putString(KEY.SERVER_URL,serverUrl)
        kvault.putInt(KEY.TOTAL_WEEK,totalWeek)
    }

}