package com.pgigi.pumpkintoolkit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.CoroutineScope

@Composable
actual fun rememberInteractiveHighlight(
    animationScope: CoroutineScope,
    position: (size: Size, offset: Offset) -> Offset
): InteractiveHighlight? = null
