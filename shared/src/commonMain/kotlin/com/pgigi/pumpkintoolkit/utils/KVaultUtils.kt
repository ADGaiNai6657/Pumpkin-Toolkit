package com.pgigi.pumpkintoolkit.utils

import com.liftric.kvault.KVault

/**
 * 创建 KVault 实例（平台特定实现）
 * - Android: 使用 EncryptedSharedPreferences（加密 SharedPreferences）
 * - iOS: 使用 Keychain
 */
expect fun createKVault(): KVault

/**
 * KVault 键值存储工具类
 *
 * 封装 KVault 提供统一的键值存取接口，支持 String / Int / Long / Float / Double / Boolean / ByteArray。
 * Android 端基于 EncryptedSharedPreferences，iOS 端基于 Keychain。
 *
 * 使用示例：
 * ```
 * KVaultUtils.putString("username", "admin")
 * val name = KVaultUtils.getString("username", "")
 * KVaultUtils.remove("username")
 * ```
 */
object KVaultUtils {

    private val vault: KVault by lazy { createKVault() }

    // ==================== String ====================

    fun putString(key: String, value: String): Boolean =
        vault.set(key = key, stringValue = value)

    fun getString(key: String): String? = vault.string(forKey = key)

    fun getString(key: String, default: String): String =
        vault.string(forKey = key) ?: default

    // ==================== Int ====================

    fun putInt(key: String, value: Int): Boolean =
        vault.set(key = key, intValue = value)

    fun getInt(key: String): Int? = vault.int(forKey = key)

    fun getInt(key: String, default: Int): Int =
        vault.int(forKey = key) ?: default

    // ==================== Long ====================

    fun putLong(key: String, value: Long): Boolean =
        vault.set(key = key, longValue = value)

    fun getLong(key: String): Long? = vault.long(forKey = key)

    fun getLong(key: String, default: Long): Long =
        vault.long(forKey = key) ?: default

    // ==================== Float ====================

    fun putFloat(key: String, value: Float): Boolean =
        vault.set(key = key, floatValue = value)

    fun getFloat(key: String): Float? = vault.float(forKey = key)

    fun getFloat(key: String, default: Float): Float =
        vault.float(forKey = key) ?: default

    // ==================== Double ====================

    fun putDouble(key: String, value: Double): Boolean =
        vault.set(key = key, doubleValue = value)

    fun getDouble(key: String): Double? = vault.double(forKey = key)

    fun getDouble(key: String, default: Double): Double =
        vault.double(forKey = key) ?: default

    // ==================== Boolean ====================

    fun putBoolean(key: String, value: Boolean): Boolean =
        vault.set(key = key, boolValue = value)

    fun getBoolean(key: String): Boolean? = vault.bool(forKey = key)

    fun getBoolean(key: String, default: Boolean): Boolean =
        vault.bool(forKey = key) ?: default

    // ==================== ByteArray ====================

    fun putByteArray(key: String, value: ByteArray): Boolean =
        vault.set(key = key, dataValue = value)

    fun getByteArray(key: String): ByteArray? = vault.data(forKey = key)

    // ==================== 通用操作 ====================

    /** 检查指定 key 是否存在 */
    fun contains(key: String): Boolean = vault.existsObject(forKey = key)

    /** 删除指定 key */
    fun remove(key: String): Boolean = vault.deleteObject(forKey = key)

    /** 清空所有存储 */
    fun clear(): Boolean = vault.clear()

    /** 获取所有已存储的 key */
    fun keys(): List<String> = vault.allKeys()
}
