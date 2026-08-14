package com.pgigi.pumpkintoolkit

import androidx.navigation3.runtime.NavKey
import com.pgigi.pumpkintoolkit.models.sunshine.GuestBookItem
import com.pgigi.pumpkintoolkit.models.sunshine.LostItem
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation keys for Navigation3.
 * Each destination is a NavKey (data object/data class) and can be saved/restored in the back stack.
 */
sealed interface Route : NavKey {
    @Serializable
    data object Home : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object OtherSchedule: Route
    @Serializable
    data class WebView(val url: String, val title: String = "") :
        Route
    @Serializable
    data class WebViewWithData(val html: String, val title: String = "") :
        Route
    @Serializable
    data object SunshineMenu : Route
    @Serializable
    data class SunshineDetail(val item: GuestBookItem): Route
    @Serializable
    data class SunshineList(val typeCode: String = "", val submitUrl: String? = null) : Route
    @Serializable
    data object LostAndFoundList : Route
    @Serializable
    data class LostAndFoundDetail(val item: LostItem) : Route
    @Serializable
    data class SimpleHtml(val html: String, val title: String = "") :Route
}
