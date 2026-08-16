package com.pgigi.pumpkintoolkit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pgigi.pumpkintoolkit.constants.FileName
import com.pgigi.pumpkintoolkit.models.ExamInfo
import com.pgigi.pumpkintoolkit.models.ExamCache
import com.pgigi.pumpkintoolkit.utils.FileStoreUtils
import com.pgigi.pumpkintoolkit.utils.JsonUtil
import kotlin.time.Clock

fun shortTermLabel(termName: String): String {
    val regex = Regex("(\\d{2})(\\d{2})-(\\d{2})(\\d{2})学年第([一二三四五六])学期")
    val match = regex.find(termName)
    if (match != null) {
        val startYearShort = match.groupValues[2]
        val semStr = when (match.groupValues[5]) {
            "一" -> "1"; "二" -> "2"; "三" -> "3"
            "四" -> "4"; "五" -> "5"; "六" -> "6"
            else -> match.groupValues[5]
        }
        return "$startYearShort-$semStr"
    }
    return termName.take(8)
}

class ExamViewModel : ViewModel() {
    val examInfoListMap = mutableStateMapOf<String, List<ExamInfo>>()
    var currentTermIndex by mutableIntStateOf(0)
    private var cacheLoaded = false

    fun loadExamCache() {
        if (cacheLoaded) return
        cacheLoaded = true
        val json = FileStoreUtils.readString(FileName.EXAM)
        json?.let {
            try {
                val cache = JsonUtil.parseJson(it, ExamCache.serializer())
                cache.exams.forEach { (termId, exams) ->
                    examInfoListMap[termId] = exams
                }
            } catch (_: Exception) {}
        }
    }

    fun saveExamCache() {
        val cache = ExamCache(
            updateTime = Clock.System.now().toEpochMilliseconds(),
            exams = examInfoListMap.toMap()
        )
        FileStoreUtils.writeString(FileName.EXAM, JsonUtil.toJson(cache, ExamCache.serializer()))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ExamViewModel()
            }
        }
    }
}
