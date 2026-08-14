package com.pgigi.pumpkintoolkit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pgigi.pumpkintoolkit.models.CoursePlan

class PlanViewModel : ViewModel() {
    var planList = mutableStateListOf<CoursePlan>()
    var selectedTerm by mutableIntStateOf(0)
    var termNameList = mutableStateListOf("全部学期")

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlanViewModel()
            }
        }
    }
}