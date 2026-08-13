package com.pgigi.pumpkintoolkit.utils

import kotlinx.datetime.*
import kotlinx.datetime.format.*

/**
 * 自定义周数计算器：周日为一周第一天（KMP 适配版）
 * 核心规则：
 * 1. 2026-03-01（周日）所在周为第1周
 * 2. 2026-02-22 ~ 2026-02-28 为第0周
 * 3. 2026-02-09 ~ 2026-02-15 为第-2周
 */
class WeekCalculator(
    baseDate: LocalDate = LocalDate(2026, 3, 1),
    baseWeek: Int = 1
) {
    // 通用日期格式化器（yyyy-MM-dd）
    private val formatter = LocalDate.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
    }

    // 基准周的第一天（周日）
    private val baseSunday: LocalDate = getSundayOfWeek(baseDate)
    // 基准周对应的周数
    private val baseWeekNum: Int = baseWeek

    /**
     * 计算目标日期所属周数（核心方法）
     * @param date 目标日期
     * @return 所属周数（支持负数、0、正数）
     */
    fun getWeekNumber(date: LocalDate): Long {
        // 先获取目标日期所在周的周日（一周第一天）
        val targetSunday = getSundayOfWeek(date)
        // 计算基准周日与目标周日的天数差
        val dayDiff = baseSunday.until(targetSunday, DateTimeUnit.DAY)
        // 天数差转周数差 + 基准周数 = 最终周数
        return dayDiff / 7 + baseWeekNum
    }

    /**
     * 重载：通过日期字符串计算周数
     * @param dateStr 日期字符串（yyyy-MM-dd）
     * @return 所属周数
     * @throws IllegalArgumentException 日期格式错误/空值时抛出
     */
    fun getWeekNumber(dateStr: String): Long {
        require(dateStr.isNotBlank()) { "日期字符串不能为null/空" }
        val date = try {
            formatter.parse(dateStr)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("日期格式错误，需为yyyy-MM-dd，错误值：$dateStr", e)
        }
        return getWeekNumber(date)
    }

    /**
     * 查询指定周数的第一天（周日）
     * @param weekNum 目标周数
     * @return 该周第一天（周日）
     */
    fun getWeekFirstDay(weekNum: Long): LocalDate {
        // 计算目标周与基准周的周数差 → 转换为天数差
        val weekDiff = weekNum - baseWeekNum
        val dayDiff = weekDiff * 7
        // 基准周日 + 天数差 = 目标周的周日（第一天）
        return baseSunday.plus(dayDiff, DateTimeUnit.DAY)
    }

    /**
     * 重载：Int 类型周数查询第一天
     * @param weekNum 目标周数（Int）
     * @return 该周第一天（周日）
     */
    fun getWeekFirstDay(weekNum: Int): LocalDate = getWeekFirstDay(weekNum.toLong())

    /**
     * 私有工具方法：获取指定日期所在周的周日（一周第一天）
     * @param date 目标日期
     * @return 该日期所在周的周日（若本身是周日则返回自身）
     */
    private fun getSundayOfWeek(date: LocalDate): LocalDate {
        // kotlinx.datetime.DayOfWeek：周日=7，周一=1，周二=2...周六=6
        val dayOfWeek = date.dayOfWeek.isoDayNumber
        // 回退天数：周日回0天，周一回1天...周六回6天
        val backDays = dayOfWeek % 7
        return date.minus(backDays, DateTimeUnit.DAY)
    }
}