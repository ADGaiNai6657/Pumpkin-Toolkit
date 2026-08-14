package com.pgigi.pumpkintoolkit.viewmodel.sunshine

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pgigi.pumpkintoolkit.models.sunshine.GuestBookInfoResponse

class SunshineDetailViewModel: ViewModel() {
    val infoMap = mutableStateMapOf<String, GuestBookInfoResponse>()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SunshineDetailViewModel()
            }
        }
    }
}