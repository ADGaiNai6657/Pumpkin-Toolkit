package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class ExamCache(
    val updateTime: Long,
    val exams: Map<String, List<ExamInfo>>
)
