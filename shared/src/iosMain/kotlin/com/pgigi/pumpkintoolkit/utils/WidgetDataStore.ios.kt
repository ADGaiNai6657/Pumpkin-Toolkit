package com.pgigi.pumpkintoolkit.utils

import com.pgigi.pumpkintoolkit.models.WidgetData
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.Foundation.NSFileManager
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIDevice

const val WIDGET_DATA_CHANGED_NOTIFICATION = "com.pgigi.pumpkintoolkit.widgetDataChanged"

private const val API_BASE_URL = "https://pumpkin-api.pgigi.com"
private const val WIDGET_DATA_FILE = "widget_data.json"
private const val APP_GROUP_ID = "group.com.pgigi.pumpkintoolkit"

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual object WidgetDataStore {

    private fun idfv(): String {
        return UIDevice.currentDevice.identifierForVendor?.UUIDString ?: ""
    }

    private fun getSharedContainerPath(): String? {
        val fileManager = NSFileManager.defaultManager
        val containerURL = fileManager.containerURLForSecurityApplicationGroupIdentifier(APP_GROUP_ID)
        return containerURL?.path
    }

    private fun getWidgetDataFilePath(): String? {
        val containerPath = getSharedContainerPath() ?: return null
        return "$containerPath/$WIDGET_DATA_FILE"
    }

    actual fun save(data: WidgetData) {
        val json = JsonUtil.toJson(data, WidgetData.serializer())
        val deviceId = idfv()
        if (deviceId.isEmpty()) return

        // 保存到本地文件（App Group共享容器）
        val filePath = getWidgetDataFilePath()
        if (filePath != null) {
            try {
                val nsString = json as NSString
                nsString.writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
            } catch (_: Exception) { }
        }

        // 云端 KV 存储（异步，不阻塞主流程）
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                val client = HttpClient()
                client.post("$API_BASE_URL/put/$deviceId") {
                    contentType(ContentType.Application.Json)
                    setBody(json)
                }
                client.close()
            } catch (_: Exception) { }
        }
    }

    actual fun load(): WidgetData? {
        // 首先尝试从本地文件加载（App Group共享容器）
        val filePath = getWidgetDataFilePath()
        if (filePath != null) {
            try {
                val jsonString = NSString.stringWithContentsOfFile(filePath, NSUTF8StringEncoding, null)
                if (jsonString != null && jsonString.length > 0) {
                    return JsonUtil.parseJson(jsonString as String, WidgetData.serializer())
                }
            } catch (_: Exception) { }
        }

        // 如果本地没有数据，返回null
        return null
    }

    /**
     * 清除小组件课表数据（调试用）
     */
    actual fun clearWidgetData() {
        val filePath = getWidgetDataFilePath()
        if (filePath != null) {
            try {
                NSFileManager.defaultManager.removeItemAtPath(filePath, error = null)
            } catch (_: Exception) { }
        }
    }
}

actual fun reloadWidgetTimelines() {
    NSNotificationCenter.defaultCenter.postNotificationName(
        WIDGET_DATA_CHANGED_NOTIFICATION,
        `object` = null
    )
}