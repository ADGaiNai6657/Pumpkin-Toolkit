package com.pgigi.pumpkintoolkit.components.animation

import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val epoch = TimeSource.Monotonic.markNow()

actual fun currentTimeMillis(): Long = epoch.elapsedNow().toLong(DurationUnit.MILLISECONDS)
