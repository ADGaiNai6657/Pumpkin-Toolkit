package com.pgigi.pumpkintoolkit.utils

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

/**
 * 包内资源读取工具类
 *
 * 基于 Compose Multiplatform Resources，从 commonMain/composeResources/files/ 读取资源。
 * 支持二进制流和文本读取，跨平台统一访问，无需分别放置 Android assets 和 iOS bundle。
 *
 * 使用示例：
 * ```
 * val text = ResourceUtils.readText("data/config.json")
 * val bytes = ResourceUtils.readBytes("data/config.json")
 * ```
 */
@OptIn(InternalResourceApi::class)
object ResourceUtils {

    private const val RESOURCE_BASE = "composeResources/pumpkintoolkit.shared.generated.resources/files/"

    /** 读取资源为文本，失败返回 null */
    fun readText(path: String): String? = try {
        runBlocking { readResourceBytes(RESOURCE_BASE + path) }.decodeToString()
    } catch (e: Exception) {
        null
    }

    /** 读取资源为二进制数据，失败返回 null */
    fun readBytes(path: String): ByteArray? = try {
        runBlocking { readResourceBytes(RESOURCE_BASE + path) }
    } catch (e: Exception) {
        null
    }

    /** 判断资源是否存在 */
    fun exists(path: String): Boolean = readBytes(path) != null
}
