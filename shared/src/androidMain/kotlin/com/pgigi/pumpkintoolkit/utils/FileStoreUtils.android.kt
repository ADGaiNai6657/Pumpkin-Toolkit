package com.pgigi.pumpkintoolkit.utils

/**
 * Android 平台应用数据目录
 * 对应 context.filesDir
 */
actual fun appDataDir(): String {
    return KVaultContextHolder.context.filesDir.absolutePath
}

/**
 * Android 平台应用缓存目录
 * 对应 context.cacheDir
 */
actual fun appCacheDir(): String {
    return KVaultContextHolder.context.cacheDir.absolutePath
}
