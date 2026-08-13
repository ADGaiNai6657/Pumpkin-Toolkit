package com.pgigi.pumpkintoolkit.constants

import com.pgigi.pumpkintoolkit.models.ScheduleTime

object TimeList {
    var summerAutumnTime = listOf(
        ScheduleTime("08:00","08:45"),
        ScheduleTime("08:55","09:40"),
        ScheduleTime("10:00","10:45"),
        ScheduleTime("10:55","11:40"),
        ScheduleTime("15:00","15:45"),
        ScheduleTime("15:55","16:40"),
        ScheduleTime("17:00","17:45"),
        ScheduleTime("17:55","18:40"),
        ScheduleTime("20:00","20:45"),
        ScheduleTime("20:55","21:40")
    )
    var winterSpringTime = listOf(
        ScheduleTime("08:20","09:05"),
        ScheduleTime("09:15","10:00"),
        ScheduleTime("10:20","11:05"),
        ScheduleTime("11:15","12:00"),
        ScheduleTime("14:30","15:15"),
        ScheduleTime("15:25","16:10"),
        ScheduleTime("16:30", "17:15"),
        ScheduleTime("17:25","18:10"),
        ScheduleTime("19:30","20:15"),
        ScheduleTime("20:25","21:10")
    )
}