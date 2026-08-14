package com.pgigi.pumpkintoolkit.viewmodel.sunshine

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
import com.pgigi.pumpkintoolkit.models.sunshine.GuestBookItem

class SunshineListViewModel: ViewModel() {
    val map = mutableStateMapOf<String, SunshineListItem>()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SunshineListViewModel()
            }
        }
    }
}

class SunshineListItem {
    val list = mutableStateListOf<GuestBookItem>()
    var pageIndex by mutableIntStateOf(1)
    var searchKey by mutableStateOf("")
    var typeCode by mutableStateOf("")
    var listEnded by mutableStateOf(false)
    var firstVisibleItemIndex by mutableIntStateOf(0)
    var firstVisibleItemScrollOffset by mutableIntStateOf(0)
}