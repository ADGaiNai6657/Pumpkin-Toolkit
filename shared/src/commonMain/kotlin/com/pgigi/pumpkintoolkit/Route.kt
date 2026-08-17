package com.pgigi.pumpkintoolkit

import top.yukonga.miuix.kmp.nav.core.NavKey
import com.pgigi.pumpkintoolkit.models.sunshine.GuestBookItem
import com.pgigi.pumpkintoolkit.models.sunshine.LostItem
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation keys for miuix-nav.
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
    @Serializable
    data object OssLicense : Route
    @Serializable
    data class OssLicenseDetail(val license: com.pgigi.pumpkintoolkit.models.OssLicense) : Route
    @Serializable
    data object EmptyRoom : Route
    @Serializable
    data object Exam : Route

    @Serializable
    data object ExamScore : Route

    @Serializable
    data object Plan: Route

    @Serializable
    data object EvaluationMenu : Route
    @Serializable
    data class EvaluationList(val actionUrl: String, val title: String = "评教列表") : Route
    @Serializable
    data class EvaluationDetail(val actionUrl: String, val title: String = "评教详情") : Route
}
