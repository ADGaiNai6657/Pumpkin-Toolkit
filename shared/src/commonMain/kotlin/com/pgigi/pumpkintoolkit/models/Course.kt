package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    var name: String,
    var classroom: String,
    var teacher: String,
    var weeks: Array<IntArray>,
    var dayOfWeek: Int, // 0-6
    var lessonOfDay: Int,
    var duration: Int = 2
) {
    constructor() : this("", "", "", Array(0) { IntArray(2) }, -1, -1)
    constructor(
        name: String,
        classroom: String,
        teacher: String,
        weeks: String,
        dayOfWeek: Int,
        lessonOfDay: Int,
        duration: Int = 2
    ) : this(name, classroom, teacher, parseWeeks(weeks), dayOfWeek, lessonOfDay, duration)

    companion object {
        private fun parseWeeks(weeks: String): Array<IntArray> {
            val weekSplit = weeks.split(",").filter { it.isNotEmpty() }
            return Array(weekSplit.size) { index ->
                val range = weekSplit[index].split("-").map { it.toInt() }
                if (range.size == 1) intArrayOf(range[0], range[0]) else intArrayOf(range[0], range[1])
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other == null || other !is Course) return false
        if(name != other.name) return false
        if(classroom != other.classroom) return false
        if(teacher != other.teacher) return false
        if(!weeks.contentDeepEquals(other.weeks)) return false
        if(dayOfWeek != other.dayOfWeek) return false
        if(lessonOfDay != other.lessonOfDay) return false
        if(duration != other.duration) return false
        return true
    }
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + classroom.hashCode()
        result = 31 * result + teacher.hashCode()
        result = 31 * result + weeks.contentDeepHashCode()
        result = 31 * result + dayOfWeek
        result = 31 * result + lessonOfDay
        result = 31 * result + duration
        return result
    }
}