package com.pgigi.pumpkintoolkit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.models.BackgroundConfig
import com.pgigi.pumpkintoolkit.utils.loadBackgroundImage
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 图片背景的高斯模糊半径。 */
private val ImageBlurRadius = 24.dp

/**
 * 当前是否铺了**自定义背景**（纯色或图片，而非「跟随系统」）。
 *
 * 半透明导航栏 / 功能面板这类 chrome 组件据此换色；跟随系统时一律 false，
 * 外观与加自定义背景之前完全一致。
 */
val LocalSceneBackdropCustom: ProvidableCompositionLocal<Boolean> =
    staticCompositionLocalOf { false }

/** 一次解析好的自定义背景：配置 + 预解码的图片 + 是否生效 + chrome 不透明度。 */
class SceneBackdropState(
    val config: BackgroundConfig?,
    val image: ImageBitmap?,
    val custom: Boolean,
    val chromeOpacity: Float,
)

/**
 * 在导航宿主处记忆一份 [SceneBackdropState]：配置或图片变化时自动重解。
 *
 * 图片只在宿主那一层解码一次，所有 entry 共用同一份位图。
 */
@Composable
fun rememberSceneBackdrop(): SceneBackdropState {
    val config = AppConfig.background
    val image = remember(config?.image) {
        config?.image?.takeIf { it.isNotEmpty() }?.let { loadBackgroundImage(it) }
    }
    // 背景读不出来（换设备/清数据）时不算「自定义」，外观回落跟随系统
    val custom = when (config?.type) {
        BackgroundConfig.TYPE_COLOR -> true
        BackgroundConfig.TYPE_IMAGE -> image != null
        else -> false
    }
    return SceneBackdropState(
        config = config,
        image = image,
        custom = custom,
        chromeOpacity = AppConfig.chromeOpacity,
    )
}

/**
 * 自定义背景下 chrome 底色（卡片 / 底栏 / 导航栏 / 面板）换成的半透明纱：
 * 浅色主题是白纱、深色主题是深灰纱，**浓度由 [chromeOpacity]（0.25..1）控制**。
 *
 * @param indicator 主题底色，只用来判断深浅模式
 */
private fun sceneVeilColor(indicator: Color, chromeOpacity: Float): Color {
    val alpha = chromeOpacity.coerceIn(0.25f, 1f)
    return if (indicator.luminance() < 0.5f) {
        Color(0xFF1C1C1E).copy(alpha = alpha)
    } else {
        Color.White.copy(alpha = alpha)
    }
}

/**
 * 卡片 / 导航栏 / 功能面板这类**内容底色**该取什么：
 *
 * - 铺了自定义背景 → 场景纱色（半透明，背景透出来），浓度 = chrome 不透明度；
 * - 跟随系统 → 原来的 [base] 不透明色，观感与加背景之前完全一致。
 */
@Composable
fun sceneChrome(base: Color): Color =
    if (LocalSceneBackdropCustom.current) sceneVeilColor(base, AppConfig.chromeOpacity) else base

/**
 * **页面底色**（登录页整屏底等）该取什么：
 *
 * - 铺了自定义背景 → 完全透明，让背景图直接透出来；
 * - 跟随系统 → 原来的 [base]。
 */
@Composable
fun scenePageColor(base: Color): Color =
    if (LocalSceneBackdropCustom.current) Color.Transparent else base

/**
 * 模态容器（底部弹层这类**压在别人头上**的层）该取什么：
 *
 * - 铺了自定义背景 → 把纱色的透明度抹平成不透明，否则低浓度时正文压不住背景；
 * - 跟随系统 → 原来的 [color]。
 */
@Composable
fun sceneOpaque(color: Color): Color =
    if (LocalSceneBackdropCustom.current) color.copy(alpha = 1f) else color

/**
 * 背景层：图片（可选模糊 + 暗色蒙层）/ 纯色 / 回落色。
 *
 * 只画背景、不画内容；调用方决定铺在什么底下。
 *
 * @param fallback 没有可绘制背景时（跟随系统 / 图片缺失）垫的颜色；
 *   [Color.Unspecified] 表示什么都不画（App 宿主用，保持原样透出根容器底色）
 */
@Composable
fun SceneBackgroundLayers(
    config: BackgroundConfig?,
    image: ImageBitmap?,
    fallback: Color = Color.Unspecified,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            config != null && config.type == BackgroundConfig.TYPE_IMAGE && image != null -> {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (config.blur) Modifier.blur(ImageBlurRadius) else Modifier)
                )
                val dim = config.clampedDim()
                if (dim > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = dim))
                    )
                }
            }

            config != null && config.type == BackgroundConfig.TYPE_COLOR -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(parseBackgroundColor(config.color))
                )
            }

            fallback.isSpecified -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(fallback)
                )
            }
        }
    }
}

/**
 * Miuix 模式的背景宿主：铺自定义背景时换一层半透明主题纱并把页面底打透明，
 * 跟随系统时结构与颜色完全不变。
 *
 * 必须包在 [MiuixTheme]（controller 版）里面——这里只覆盖颜色，
 * `LocalContentColor` / `LocalColorSchemeMode` 继续由外层提供。
 *
 * @param backdrop 由导航宿主（`App`）解析一次后传入，与 [backdropCullingTransition] 共用同一份状态
 */
@Composable
fun MiuixSceneBackdrop(backdrop: SceneBackdropState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSceneBackdropCustom provides backdrop.custom) {
        if (!backdrop.custom) {
            content()
        } else {
            val base = MiuixTheme.colorScheme
            val veil = sceneVeilColor(base.background, backdrop.chromeOpacity)
            MiuixTheme(
                colors = base.copy(
                    // 页面底 / 顶栏默认色 → 透明，背景直接透出来
                    surface = Color.Transparent,
                    // 卡片 / 底栏 / 下拉弹层 → 半透明纱（浓度 = 不透明度滑块）
                    surfaceContainer = veil,
                    // 圆形浮标等 surfaceVariant 底 → 半透明纱
                    surfaceVariant = veil,
                    // background（对话框 / 底部弹层）保持不透明
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SceneBackgroundLayers(config = backdrop.config, image = backdrop.image)
                    content()
                }
            }
        }
    }
}

/**
 * Material3 模式的背景宿主：与 [MiuixSceneBackdrop] 同一套策略——
 * `background`（Scaffold 页底）/ `surface`（顶栏）打透明，
 * `surfaceVariant` / `surfaceContainer` / `surfaceContainerLow` / `surfaceContainerHighest`
 * 换半透明纱；`surfaceContainerHigh`（对话框 / 弹层）保持不透明。
 *
 * @param backdrop 由导航宿主（`App`）解析一次后传入，与 [backdropCullingTransition] 共用同一份状态
 */
@Composable
fun Material3SceneBackdrop(backdrop: SceneBackdropState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSceneBackdropCustom provides backdrop.custom) {
        if (!backdrop.custom) {
            content()
        } else {
            val base = MaterialTheme.colorScheme
            val veil = sceneVeilColor(base.background, backdrop.chromeOpacity)
            val scheme = base.copy(
                background = Color.Transparent,   // Scaffold 页面底
                surface = Color.Transparent,      // 顶栏
                surfaceVariant = veil,            // 彩色卡片
                surfaceContainer = veil,          // 分组卡片 / 底部导航
                surfaceContainerLow = veil,       // 轻卡片 / 底部弹层（弹层调用处抹平透明度）
                surfaceContainerHighest = veil,   // 默认 Card
            )
            MaterialTheme(colorScheme = scheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SceneBackgroundLayers(config = backdrop.config, image = backdrop.image)
                    content()
                }
            }
        }
    }
}

/**
 * 自定义背景专用的转场包装：把 [NavTransition.opaqueDepth] 从 1f 压到 [CULL_BELOW_ONE]。
 *
 * 导航的可见窗口规则是「静止时保留一层被覆盖的下层页面」（`d <= opaqueDepth`，静止时被覆盖页
 * 恰好 d == 1f），它依赖**顶层页面不透明**把下层完全遮住。自定义背景把页面底改成透明后，
 * 下层页面会透过来与背景叠显（主页的底栏、列表直接浮在设置页上）。
 *
 * 压到 1f 以下后：静止时只渲染顶层页面（下层 ViewModel / 可保存状态仍被保留，返回时恢复）；
 * 转场期间 d < 1，被覆盖页依旧正常显示，动画观感不变。**未启用背景时不使用本包装**。
 */
fun backdropCullingTransition(base: NavTransition): NavTransition {
    if (base.opaqueDepth < 1f) return base
    return object : NavTransition by base {
        override val opaqueDepth: Float = CULL_BELOW_ONE
    }
}

/** 略小于 1 的可见窗口深度：静止时 d == 1f 的被覆盖页被剔除，转场末尾 0.1% 的缝隙不可感知。 */
private const val CULL_BELOW_ONE = 0.999f

/**
 * 解析背景色：支持 `#RGB` / `#RRGGBB` / `#AARRGGBB`（可带 `#` 前缀，忽略大小写与空白）。
 *
 * 认不出来时回落 [BackgroundConfig.DEFAULT_COLOR]——配置文件被手改成乱码也不至于没背景。
 */
fun parseBackgroundColor(hex: String): Color {
    val cleaned = hex.trim().removePrefix("#").lowercase()
    val digits = when (cleaned.length) {
        // #RGB → RRGGBB
        3 -> cleaned.map { "$it$it" }.joinToString("")
        6 -> "ff$cleaned"
        8 -> cleaned
        else -> return Color(0xFFF2F2F7)
    }
    val value = digits.toLongOrNull(16) ?: return Color(0xFFF2F2F7)
    return Color(
        red = ((value ushr 16) and 0xFF) / 255f,
        green = ((value ushr 8) and 0xFF) / 255f,
        blue = (value and 0xFF) / 255f,
        alpha = ((value ushr 24) and 0xFF) / 255f
    )
}

/** 输入框用：是否是合法的背景色写法（`#RGB` / `#RRGGBB` / `#AARRGGBB`）。 */
fun isValidBackgroundColor(hex: String): Boolean {
    val cleaned = hex.trim().removePrefix("#")
    return cleaned.length in setOf(3, 6, 8) && cleaned.all { it in "0123456789abcdefABCDEF" }
}
