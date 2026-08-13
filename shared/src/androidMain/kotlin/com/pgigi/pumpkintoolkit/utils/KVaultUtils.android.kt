package com.pgigi.pumpkintoolkit.utils

import android.content.Context
import com.liftric.kvault.KVault

/**
 * Android Context 持有器
 *
 * 需在 Application 或 Activity 启动时初始化：
 * ```
 * KVaultContextHolder.context = applicationContext
 * ```
 */
object KVaultContextHolder {
    lateinit var context: Context
}

/**
 * Android 平台创建 KVault 实例
 * 使用 EncryptedSharedPreferences 实现加密存储
 */
actual fun createKVault(): KVault {
    return KVault(context = KVaultContextHolder.context, fileName = "config")
}
