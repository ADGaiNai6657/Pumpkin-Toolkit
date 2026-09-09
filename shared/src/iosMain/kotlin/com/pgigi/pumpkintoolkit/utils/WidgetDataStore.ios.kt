package com.pgigi.pumpkintoolkit.utils

import com.pgigi.pumpkintoolkit.models.WidgetData
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSFileManager
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaults

private const val APP_GROUP = "group.com.pgigi.pumpkintoolkit"
private const val WIDGET_DATA_KEY = "widget_data"
private const val WIDGET_DATA_FILE = "widget_data.json"
const val WIDGET_DATA_CHANGED_NOTIFICATION = "com.pgigi.pumpkintoolkit.widgetDataChanged"

actual object WidgetDataStore {

    private fun containerPath(): String? {
        return NSFileManager.defaultManager
            .containerURLForSecurityApplicationGroupIdentifier(APP_GROUP)
            ?.path
    }

    actual fun save(data: WidgetData) {
        val json = JsonUtil.toJson(data, WidgetData.serializer())

        NSUserDefaults(suiteName = APP_GROUP)?.let { defaults ->
            defaults.setObject(json, forKey = WIDGET_DATA_KEY)
            defaults.synchronize()
        }

        val dir = containerPath()
        if (dir != null) {
            val filePath = "$dir/$WIDGET_DATA_FILE".toPath()
            FileSystem.SYSTEM.write(filePath) {
                write(json.encodeToByteArray())
            }
        }
    }

    actual fun load(): WidgetData? {
        val dir = containerPath()
        if (dir != null) {
            val filePath = "$dir/$WIDGET_DATA_FILE".toPath()
            if (FileSystem.SYSTEM.exists(filePath)) {
                val json = FileSystem.SYSTEM.read(filePath) {
                    readUtf8()
                }
                return try {
                    JsonUtil.parseJson(json, WidgetData.serializer())
                } catch (_: Exception) { null }
            }
        }

        val json = NSUserDefaults(suiteName = APP_GROUP)
            ?.stringForKey(WIDGET_DATA_KEY) as? String ?: return null
        return try {
            JsonUtil.parseJson(json, WidgetData.serializer())
        } catch (_: Exception) { null }
    }
}

actual fun reloadWidgetTimelines() {
    NSNotificationCenter.defaultCenter.postNotificationName(
        WIDGET_DATA_CHANGED_NOTIFICATION,
        `object` = null
    )
}
