package com.pgigi.pumpkintoolkit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgigi.pumpkintoolkit.models.BackgroundConfig
import com.pgigi.pumpkintoolkit.utils.rememberImageFilePicker
import com.pgigi.pumpkintoolkit.utils.saveBackgroundImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 背景类型下拉 / 选择器的展示文案（下标与 [backgroundTypeAt] 对应）。 */
val BackgroundTypeLabels = listOf("跟随系统", "纯色", "图片")

/** 当前配置对应的类型下标（0 跟随系统 / 1 纯色 / 2 图片）。 */
fun backgroundTypeIndexOf(config: BackgroundConfig?): Int = when (config?.type) {
    BackgroundConfig.TYPE_COLOR -> 1
    BackgroundConfig.TYPE_IMAGE -> 2
    else -> 0
}

/** 下标 → 类型常量。 */
fun backgroundTypeAt(index: Int): String = when (index) {
    1 -> BackgroundConfig.TYPE_COLOR
    2 -> BackgroundConfig.TYPE_IMAGE
    else -> BackgroundConfig.TYPE_SYSTEM
}

/** 预设色板：两行五列，前 6 个是中性灰、后 4 个是点缀色。 */
val BackgroundPalette = listOf(
    "#FFFFFF", "#F2F2F7", "#D1D1D6", "#1C1C1E", "#3A3A3C",
    "#0A84FF", "#34C759", "#FF9F0A", "#FF375F", "#BF5AF2"
)

/**
 * 实时预览条：把当前这份背景（含蒙层 / 模糊）原样铺一遍，左下角标注类型。
 *
 * @param fallback 跟随系统 / 图片缺失时垫的底色（一般是各主题的页面底色）
 */
@Composable
fun BackgroundPreviewStrip(
    config: BackgroundConfig?,
    image: ImageBitmap?,
    fallback: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
    ) {
        SceneBackgroundLayers(config = config, image = image, fallback = fallback)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            val label = when {
                config?.type == BackgroundConfig.TYPE_IMAGE && image != null -> "图片背景"
                config?.type == BackgroundConfig.TYPE_IMAGE -> "图片缺失"
                config?.type == BackgroundConfig.TYPE_COLOR -> "纯色 ${config.color.uppercase()}"
                else -> "跟随系统"
            }
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 色板：两行五列圆点，选中的用描边高亮，点击回写配置。
 */
@Composable
fun BackgroundColorPalette(
    selected: String,
    onSelect: (String) -> Unit,
    selectedBorder: Color,
    normalBorder: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BackgroundPalette.chunked(5).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowColors.forEach { hex ->
                    val isSelected = selected.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(parseBackgroundColor(hex))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) selectedBorder else normalBorder,
                                shape = CircleShape,
                            )
                            .clickable { onSelect(hex) }
                    )
                }
            }
        }
    }
}

/**
 * 记忆一个「选图 → 后台降采样压 JPEG → 落库」的启动器（两套设置页共用）。
 *
 * @param onSaved 落库成功，参数是相对应用数据目录的文件名
 * @param onError 解码 / 写入失败
 * @return 启动系统选择器的函数；用户取消时什么都不做
 */
@Composable
fun rememberBackgroundImagePicker(
    onSaved: (String) -> Unit,
    onError: () -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    return rememberImageFilePicker { raw ->
        if (raw != null) {
            scope.launch {
                val saved = withContext(Dispatchers.Default) { saveBackgroundImage(raw) }
                if (saved != null) onSaved(saved) else onError()
            }
        }
    }
}
