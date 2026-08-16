package com.pgigi.pumpkintoolkit.components

import android.os.Build

actual fun isLiquidGlassSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
