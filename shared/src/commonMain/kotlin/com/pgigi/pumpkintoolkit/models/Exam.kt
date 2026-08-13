package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class Exam(
    var courseId: String = "", // 课程编号
    var name: String = "", // 课程名称
    var week: String = "", // 考试周次
    var day: String = "", // 考试星期
    var lesson: String = "", // 考试节次
    var time: String = "", // 考试时间
    var room: String = "", // 考试地点
    var school: String = "", // 校区
    var type: String = "", // 考试类型
    var seat: String = "", // 座位号
    var admissionNum: String = "", // 准考证号
    var comment: String = "" // 备注
){
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other == null || other !is Exam) return false
        if(courseId != other.courseId) return false
        if(name != other.name) return false
        if(week != other.week) return false
        if(day != other.day) return false
        if(lesson != other.lesson) return false
        if(time != other.time) return false
        if(room != other.room) return false
        if(school != other.school) return false
        if(type != other.type) return false
        if(seat != other.seat) return false
        if(admissionNum != other.admissionNum) return false
        if(comment != other.comment) return false
        return true
    }
    override fun hashCode(): Int {
        var result = courseId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + week.hashCode()
        result = 31 * result + day.hashCode()
        result = 31 * result + lesson.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + room.hashCode()
        result = 31 * result + school.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + seat.hashCode()
        result = 31 * result + admissionNum.hashCode()
        result = 31 * result + comment.hashCode()
        return result
    }
}