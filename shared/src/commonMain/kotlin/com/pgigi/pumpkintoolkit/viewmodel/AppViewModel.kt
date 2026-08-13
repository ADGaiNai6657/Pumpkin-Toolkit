package com.pgigi.pumpkintoolkit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.models.Course

class AppViewModel : ViewModel(){
    val backStack = mutableStateListOf<NavKey>(Route.Home)
    val courseList = mutableStateListOf<Course>()
    var currentWeek by mutableIntStateOf(1)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppViewModel()
            }
        }
    }
}