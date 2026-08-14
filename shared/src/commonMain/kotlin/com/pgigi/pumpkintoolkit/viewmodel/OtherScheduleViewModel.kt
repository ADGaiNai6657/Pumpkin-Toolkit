package com.pgigi.pumpkintoolkit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.models.Course
import kotlinx.datetime.LocalDate

class OtherScheduleViewModel : ViewModel(){
    val startDateMap = mutableStateMapOf<String, LocalDate>()
    val courseListMap = mutableStateMapOf<String, List<Course>>()
    var currentTermIndex by mutableIntStateOf(0)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                OtherScheduleViewModel()
            }
        }
    }
}