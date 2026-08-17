package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

@Serializable
data class OssLicense(
    val title: String,
    val author: String,
    val link: String,
    val licence: String,
    val file: String? = null,
    val group: String? = null
)

fun getLicenseUrl(licenseName: String): String? = when {
    licenseName.contains("Apache", ignoreCase = true) -> "https://www.apache.org/licenses/LICENSE-2.0"
    licenseName.contains("MIT", ignoreCase = true) -> "https://opensource.org/licenses/MIT"
    else -> null
}
