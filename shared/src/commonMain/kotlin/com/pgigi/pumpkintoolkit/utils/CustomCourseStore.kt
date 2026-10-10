package com.pgigi.pumpkintoolkit.utils

import com.pgigi.pumpkintoolkit.constants.FileName
import com.pgigi.pumpkintoolkit.models.CustomCourse
import com.pgigi.pumpkintoolkit.models.CustomScheduleCache

object CustomCourseStore {

    //加载函数，目的是从持久化的json中解析出课表数据
    fun load(): List<CustomCourse> {

        //使用工具函数读取custom_schedule.json下的数据。没有/解析失败？返回空列表。 *记得判空
        val json = FileStoreUtils.readString(FileName.CUSTOM_SCHEDULE)
            ?: return emptyList()

        //尝试进行解析。将拆解出的courses列表进行返回
        try {

            val cache = JsonUtil.parseJson(json, CustomScheduleCache.serializer())
            return cache.courses

        } catch (_: Exception) {    //抛异常了？直接返回空列表
            return emptyList()
        }

    }

}