package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleCache(
    val updateTime: Long,
    val courses: List<Course>
)
