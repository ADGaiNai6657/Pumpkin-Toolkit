package com.pgigi.pumpkintoolkit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.CoroutineScope

interface InteractiveHighlight {
    val modifier: Modifier
    val gestureModifier: Modifier
}

@Composable
expect fun rememberInteractiveHighlight(
    animationScope: CoroutineScope,
    position: (size: Size, offset: Offset) -> Offset = { _, offset -> offset }
): InteractiveHighlight?
