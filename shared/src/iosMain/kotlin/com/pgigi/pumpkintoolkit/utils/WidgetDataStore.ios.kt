package com.pgigi.pumpkintoolkit.utils

import com.pgigi.pumpkintoolkit.models.WidgetData
import platform.Foundation.NSUserDefaults
import platform.WidgetKit.WidgetCenter

private const val APP_GROUP = "group.com.pgigi.pumpkintoolkit"
private const val WIDGET_DATA_KEY = "widget_data"

actual object WidgetDataStore {

    actual fun save(data: WidgetData) {
        val json = JsonUtil.toJson(data, WidgetData.serializer())
        NSUserDefaults(suiteName = APP_GROUP)?.let { defaults ->
            defaults.setObject(json, forKey = WIDGET_DATA_KEY)
            defaults.synchronize()
        }
    }

    actual fun load(): WidgetData? {
        val json = NSUserDefaults(suiteName = APP_GROUP)
            ?.stringForKey(WIDGET_DATA_KEY) as? String ?: return null
        return try {
            JsonUtil.parseJson(json, WidgetData.serializer())
        } catch (_: Exception) { null }
    }
}

actual fun reloadWidgetTimelines() {
    WidgetCenter.shared.reloadAllTimelines()
}
