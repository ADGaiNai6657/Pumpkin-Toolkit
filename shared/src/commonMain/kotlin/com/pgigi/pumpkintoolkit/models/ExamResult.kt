package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class ExamResult(
    var name: String = "",
    var credit: Float = 0f,
    var grade: Float = 0f,
    var score: String = "",
    var totalClassHours: Int = 0
)