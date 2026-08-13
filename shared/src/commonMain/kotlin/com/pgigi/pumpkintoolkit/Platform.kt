package com.pgigi.pumpkintoolkit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform