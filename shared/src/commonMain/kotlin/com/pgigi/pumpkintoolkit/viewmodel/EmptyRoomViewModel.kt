package com.pgigi.pumpkintoolkit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pgigi.pumpkintoolkit.screens.miuix.Building

class EmptyRoomViewModel : ViewModel() {

    val buildings = mutableStateListOf<Building>()
    var selectedSchool by mutableIntStateOf(0)
    val buildingItems = mutableStateListOf("请选择")
    val buildingList = mutableStateListOf("")
    var selectedBuilding by mutableIntStateOf(0)
    var selectedLesson by mutableIntStateOf(0)
    val emptyRoomMap = mutableStateMapOf<String, Boolean>()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EmptyRoomViewModel()
            }
        }
    }
}