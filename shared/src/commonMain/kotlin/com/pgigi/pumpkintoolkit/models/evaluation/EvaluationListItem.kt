package test.xspj

data class EvaluationListItem(
    val courseName: String,
    val teacher: String,
    val score: Float,
    val isSubmit: Boolean,
    val actionUrl: String
)
