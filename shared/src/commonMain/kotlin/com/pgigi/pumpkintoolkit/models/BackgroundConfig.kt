package com.pgigi.pumpkintoolkit.models

import kotlinx.serialization.Serializable

/**
 * 自定义背景配置（「设置 → 主题设置 → 自定义背景」，**全局唯一一份**）。
 *
 * 没有配置或 [type] 为 [TYPE_SYSTEM] 时跟随系统主题，外观与加自定义背景之前完全一致。
 *
 * @property type 背景类型：[TYPE_SYSTEM] 跟随系统 / [TYPE_COLOR] 纯色 / [TYPE_IMAGE] 图片
 * @property color [TYPE_COLOR] 的颜色：`#RGB` / `#RRGGBB` / `#AARRGGBB`
 * @property image [TYPE_IMAGE] 的图片文件名（相对应用数据目录，见 `FileStoreUtils`）。
 *   图片在**导入时**就被降采样压成 JPEG 存进应用私有目录，配置里只存文件名，
 *   换设备/清数据后文件不存在会自动回落跟随系统
 * @property dim 图片之上的暗色蒙层强度 `0f..0.85f`：背景太花时压暗，保证文字可读
 * @property blur 图片是否做高斯模糊（同样是为了文字可读）
 */
@Serializable
data class BackgroundConfig(
    val type: String = TYPE_SYSTEM,
    val color: String = DEFAULT_COLOR,
    val image: String? = null,
    val dim: Float = 0.35f,
    val blur: Boolean = false
) {

    /** 按类型取蒙层强度（归一化到 0..0.85，脏数据兜底）。 */
    fun clampedDim(): Float = dim.coerceIn(0f, 0.85f)

    companion object {
        /** 跟随应用主题背景（等于不自定义）。 */
        const val TYPE_SYSTEM = "system"

        /** 纯色背景，取 [color]。 */
        const val TYPE_COLOR = "color"

        /** 图片背景，取 [image]。 */
        const val TYPE_IMAGE = "image"

        /** 默认纯色：与系统浅色分组底色接近的浅灰。 */
        const val DEFAULT_COLOR = "#F2F2F7"
    }
}
