package com.pgigi.pumpkintoolkit.models

data class CoursePlan(
    var name: String = "", // 课程名称
    var term: String = "", // 开课学期
    var num: String = "", // 课程编号
    var institution: String = "", // 开课单位
    var credit: Float = 0f, // 学分
    var hours: Int = 0, // 总学时
    var evaluation: String = "", // 考核方式
    var nature: String = "", // 课程性质
    var attr: String = "", // 课程属性
    var exam: String = "" // 是否考试
)