package test.xspj

import kotlinx.serialization.Serializable

@Serializable
data class EvaluationMenuItem(
    val termName: String,
    val evaluationName: String,
    val actionUrl: String
)
