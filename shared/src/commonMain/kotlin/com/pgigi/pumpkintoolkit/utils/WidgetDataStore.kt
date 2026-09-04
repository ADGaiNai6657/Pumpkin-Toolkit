package com.pgigi.pumpkintoolkit.utils

import com.pgigi.pumpkintoolkit.models.WidgetData

expect object WidgetDataStore {

    fun save(data: WidgetData)

    fun load(): WidgetData?
}

expect fun reloadWidgetTimelines()
