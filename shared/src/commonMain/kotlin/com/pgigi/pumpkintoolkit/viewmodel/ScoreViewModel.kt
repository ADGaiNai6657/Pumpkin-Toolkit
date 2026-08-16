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
import com.pgigi.pumpkintoolkit.models.ExamScore
import com.pgigi.pumpkintoolkit.models.ScoreCache
import com.pgigi.pumpkintoolkit.utils.FileStoreUtils
import com.pgigi.pumpkintoolkit.utils.JsonUtil
import kotlin.time.Clock

class ScoreViewModel : ViewModel() {
    val scoreListMap = mutableStateMapOf<String, List<ExamScore>>()
    var currentTermIndex by mutableIntStateOf(0)
    private var cacheLoaded = false

    fun loadScoreCache() {
        if (cacheLoaded) return
        cacheLoaded = true
        val json = FileStoreUtils.readString(FileName.SCORE)
        json?.let {
            try {
                val cache = JsonUtil.parseJson(it, ScoreCache.serializer())
                cache.scores.forEach { (termId, scores) ->
                    scoreListMap[termId] = scores
                }
            } catch (_: Exception) {}
        }
    }

    fun saveScoreCache() {
        val cache = ScoreCache(
            updateTime = Clock.System.now().toEpochMilliseconds(),
            scores = scoreListMap.toMap()
        )
        FileStoreUtils.writeString(FileName.SCORE, JsonUtil.toJson(cache, ScoreCache.serializer()))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ScoreViewModel()
            }
        }
    }
}
