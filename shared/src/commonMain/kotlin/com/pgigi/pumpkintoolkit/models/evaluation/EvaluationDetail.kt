package com.pgigi.pumpkintoolkit.models.evaluation

data class EvaluationItem(
    val title: String,
    val evaluationId: String,
    val scoreKeyValueMap: Map<String, Float>
)

data class EvaluationDetail(
    val keyValueMap: Map<String, String>,
    val evaluationIdSet : Set<String>,
    val evaluationList: List<EvaluationItem>,
    val comment: String
)
