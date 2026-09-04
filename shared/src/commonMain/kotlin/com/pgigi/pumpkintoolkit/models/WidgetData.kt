package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class WidgetData(
    val startDate: String?,
    val totalWeek: Int,
    val timeList: List<WidgetScheduleTime>,
    val courses: List<WidgetCourseItem>,
    val updateTime: Long
)

@Serializable
data class WidgetCourseItem(
    val name: String,
    val classroom: String,
    val teacher: String,
    val dayOfWeek: Int,
    val lessonOfDay: Int,
    val duration: Int,
    val weeks: List<List<Int>>
)

@Serializable
data class WidgetScheduleTime(
    val start: String,
    val end: String
)
