package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class ExamInfo(
    /** 课程名称 */
    val courseName: String,
    /** 考试周次 */
    val examWeek: Int,
    /** 考试星期（1=周一, 7=周日） */
    val examDayOfWeek: Int,
    /** 考试时间原始字符串，如 "2026-07-02 10:00~11:00" */
    val examTimeRaw: String,
    /** 考试地点 */
    val examLocation: String,
    /** 校区 */
    val campus: String,
    /** 座位号（kscx2 无此字段，可能为 null） */
    val seatNumber: Int? = null,
)