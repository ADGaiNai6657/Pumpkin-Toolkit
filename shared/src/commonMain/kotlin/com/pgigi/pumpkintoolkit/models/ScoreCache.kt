package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class ScoreCache(
    val updateTime: Long,
    val scores: Map<String, List<ExamScore>>
)
