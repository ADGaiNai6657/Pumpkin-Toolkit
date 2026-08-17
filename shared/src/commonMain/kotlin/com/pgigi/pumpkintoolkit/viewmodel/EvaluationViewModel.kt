package com.pgigi.pumpkintoolkit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import test.xspj.EvaluationListItem
import test.xspj.EvaluationMenuItem

class EvaluationViewModel : ViewModel() {
    val menuList = mutableStateListOf<EvaluationMenuItem>()
    var menuLoaded by mutableStateOf(false)

    val listMap = mutableStateMapOf<String, List<EvaluationListItem>>()

    companion object {
        val Factory = viewModelFactory {
            initializer { EvaluationViewModel() }
        }
    }
}
