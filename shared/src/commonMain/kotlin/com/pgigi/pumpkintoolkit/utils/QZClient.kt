package com.pgigi.pumpkintoolkit.utils

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.models.Course
import com.pgigi.pumpkintoolkit.models.ExamResult
import com.pgigi.pumpkintoolkit.qzrc.preprocessImage
import com.pgigi.pumpkintoolkit.qzrc.recognizeCaptcha
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPath
import io.ktor.http.setCookie
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

object QZClient {
    val client = HttpClient {
        followRedirects = false
    }
    
    var username = ""
    var password = ""

    var loginRedirectUrl = ""
    var logged = false

    val cookies = mutableListOf<Cookie>()

    private val loginSuccessCallbackList = mutableListOf<(String)->Unit>()

    // 设置回调
    fun addLoginSuccessCallback(callback: (jwQuickUrl: String) -> Unit) {
        this.loginSuccessCallbackList.add(callback)
    }

    // 移除回调
    fun removeLoginSuccessCallback(callback: (jwQuickUrl: String) -> Unit) {
        loginSuccessCallbackList.remove(callback)
    }

    private fun doLoginSuccessCallback(jwQuickUrl: String){
        loginSuccessCallbackList.forEach {
            it.invoke(jwQuickUrl)
        }
    }


    private fun List<Cookie>.toCookieString() : String {
        return this.joinToString("; ") { "${it.name}=${it.value}" }
    }
    private fun url(path: String): String {
        return "${AppConfig.serverUrl.trimEnd('/')}/${path.trimStart('/')}"
    }

    suspend fun login(onFailure: (reason: String) -> Unit = {}, onSuccess: (jwQuickUrl: String) -> Unit = {}): Boolean {
        try{
            val cookieList = mutableListOf<Cookie>()
            var response = client.get(url("verifycode.servlet"))
            if (response.status != HttpStatusCode.OK) {
                onFailure("获取图片验证码失败,请检查网络")
                return false
            }
            cookieList.addAll(response.setCookie())
            val verifyCode = recognizeCaptcha(preprocessImage(response.bodyAsBytes()))

            println(verifyCode)

            if (verifyCode.isEmpty()) {
                onFailure("获取图片验证码失败,请检查网络")
                return false
            }
            response = client.post(url("Logon.do?method=logon")){
                header(HttpHeaders.Cookie, cookieList.toCookieString())
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody((
                        "userAccount=${username}" +
                                "&userPassword=${password}" +
//                "&encoded=${Base64.encode(username.encodeToByteArray())}%%%" +
//                            Base64.encode(password.encodeToByteArray()) +
                                "&RANDOMCODE=$verifyCode")
                    .encodeURLPath()
                )
            }
            if (response.status != HttpStatusCode.Found) {
                val html = response.bodyAsText()
                val doc: Document = Ksoup.parse(html)
                val error = doc.select("#showMsg").first()?.text() ?: "未知错误"
                onFailure(error)
                return false
            }
            val location = response.headers[HttpHeaders.Location]?:""
            if (location.trim { it.isWhitespace() }.isEmpty()) {
                onFailure("请联系开发者")
                println("1")
                return false
            }
            response = client.get(location) {
                header(HttpHeaders.Cookie, cookieList.toCookieString())
            }
            if(response.status != HttpStatusCode.Found){
                onFailure("请联系开发者: Code="+response.status)
                return false
            }
            if(response.setCookie().isEmpty()){
                onFailure("获取登录凭证失败，请联系开发者")
                return false
            }
            cookieList.addAll(response.setCookie())
            cookies.clear()
            cookies.addAll(cookieList)
            loginRedirectUrl = location
            logged = true
            doLoginSuccessCallback(location)
            onSuccess(location)
            return true
        }catch (e: Exception){
            when(e){
                is ConnectTimeoutException -> {
                    onFailure("连接超时，请检查网络或教务系统")
                    return false
                }
                is SocketTimeoutException -> {
                    onFailure("连接超时，请检查网络或教务系统")
                    return false
                }
                is HttpRequestTimeoutException -> {
                    onFailure("连接超时，请检查网络或教务系统")
                    return false
                }
                else -> {
                    onFailure("未知错误，请检查网络或教务系统\n${e.message}")
                    return false
                }
            }
        }
    }

    suspend fun getHtml(url: String): String?{
        try{
            var retry = 0
            while(retry<=3){
                if(!logged) login(
                    { retry++ })
                if(logged){
                    val html = client.get(url) {
                        header(HttpHeaders.Cookie, cookies.toCookieString())
                    }.bodyAsText()
                    val doc = Ksoup.parse(html)
                    if(doc.title().trim()=="登录") {
                        logged = false
                        login({ retry++ })
                        continue
                    }
                    return html
                }
            }
            return null
        }catch (_: Exception){
            return null
        }
    }

    fun parseCourses(html: String): List<Course>? {
        if (html.trim().isEmpty()) return null
        val courses = mutableListOf<Course>()
        val doc = Ksoup.parse(html.replace("&nbsp;",""))
        doc.select("span").remove()
        val table = doc.select("#kbtable tbody")
        try{
            val trs = table.select("tr")
            for(i in 1..5){
                val tds = trs[i].select("td")
                for(j in 0..6){
                    val td = tds[j].selectFirst("div.kbcontent") ?: continue
                    if(td.html().isEmpty()) continue

                    val courseHtmlList = td.html().split("---------------------")
                    for(courseHtml in courseHtmlList){
                        val courseDoc = Ksoup.parse(courseHtml)
                        val teacher = courseDoc.selectFirst("font[title=老师]")?.text() ?: ""
                        val classroom = courseDoc.selectFirst("font[title=教室]")?.text() ?: ""
                        val time = courseDoc.selectFirst("font[title=周次(节次)]")?.text() ?: ""
                        val weeks = time.substringBefore("(")
                        courseDoc.select("br").remove()
                        courseDoc.select("font").remove()
                        courseDoc.select("a").remove()
                        val courseName = courseDoc.text()
                            .replace("（", "(")
                            .replace("）", ")")
                            .trim()

                        val course = Course(
                            name = courseName,
                            classroom = classroom,
                            teacher = teacher,
                            weeks = weeks,
                            dayOfWeek = j,
                            lessonOfDay = 2*i-1
                        )
                        courses.add(course)
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            return null
        }
        return courses
    }
    fun parseExamResults(html: String): List<ExamResult>? {
        if (html.trim().isEmpty()) return null
        val doc = Ksoup.parse(html)
        val resElements = doc.select("table#dataList tbody tr")
        val results = mutableListOf<ExamResult>()
        if (resElements.isEmpty()) return results
        for (elem in resElements) {
            val tds = elem.select("td")
            if (tds.size < 10) continue
            val name = tds[3].html()
            val score = tds[5].html()
            val credit: Float
            val totalClassHours: Int
            val gradePoint: Float
            try {
                credit = tds[7].html().toFloat()
                totalClassHours = tds[8].html().toInt()
                gradePoint = tds[9].html().toFloat()
            } catch (_: Exception) {
                continue
            }
            results.add(ExamResult(name, credit, gradePoint, score, totalClassHours))
        }
        return results
    }
    fun parseExperimentCourses(html:String):List<Course>?{
        if(html.trim{ it.isWhitespace()}.isEmpty()) return null
        val courses = mutableListOf<Course>()
        val doc = Ksoup.parse(html.replace("&nbsp;", " "))
        val table = doc.select("table#tblHead tbody tr")
        if (table.isEmpty()) return courses
        for(i in 1..<table.size){
            val tdElements = table[i].select("td")
            if(tdElements.size<7) continue
            val week = (i + 4) / 5
            for(j in 1..7){
                val td = tdElements[j]
                if(!td.html().contains("<br>")) continue
                val texts = td.html()
                    .replace("\n","")
                    .replace("<br>","\n")
                    .trim().split("\n".toRegex())
                val course = Course(
                    name = texts[0],
                    teacher = "",
                    classroom = texts[1].split(" ")[1],
                    weeks = week.toString(),
                    dayOfWeek = j - 1,
                    lessonOfDay = (i%5)*2-1,
                    duration = 2
                )
                courses.add(course)
            }
        }
        return courses
    }
    suspend fun getEmptyRoomsHtml(termId: String, buildingId: String, day: Int, week: Int, lesson: Int): String? {
        return getHtml(
            url("jsxsd/kbcx/kbxx_classroom_ifr") +
                    "?xnxqh=${termId}" +
                    "&jzwid=${buildingId}" +
                    "&zc1=${week}&zc2=${week}" +
                    "&skxq1=${day}&skxq2=${day}" +
                    "&jc1=${lesson}&jc2=${lesson}"
        )
    }
    suspend fun getCoursesHtml(termId: String = ""): String? {
        return getHtml(url("jsxsd/xskb/xskb_list.do?xnxq01id=$termId"))
    }
    suspend fun getExperimentCoursesHtml(termId: String = ""): String? {
        return getHtml(url("jsxsd/syjx/toXskb.do?xnxq01id=$termId"))
    }
    suspend fun getExamResultHtml(termId: String = "", display: String="all"): String? {
        return getHtml(url("jsxsd/kscj/cjcx_list?kksj=${termId}&xsfs=${display}"))
    }

    suspend fun getScheduleHtml(termId: String = ""): String?{
        return getHtml(url("jsxsd/jxzl/jxzl_query?xnxq01id=$termId"))
    }

    suspend fun getAllCourses(termId: String = ""): List<Course>?{
        val courses = mutableListOf<Course>()
        val cHtml = getCoursesHtml(termId)
        println(cHtml)
        val eHtml = getExperimentCoursesHtml(termId)
        if(cHtml.isNullOrEmpty() || eHtml.isNullOrEmpty()) return null
        val cCourses = parseCourses(cHtml)
        val eCourses = parseExperimentCourses(eHtml)
        if(cCourses == null || eCourses == null) return null
        courses.addAll(cCourses)
        courses.addAll(eCourses)
        return courses
    }

    fun parseStartDate(html: String): LocalDate?{
        val doc = Ksoup.parse(html)
        val trElements = doc.select("#kbtable tbody tr")
        if(trElements.size <= 2) return null
        val startTimeStr = trElements[1].select("td")[1].attr("title")
        val format = LocalDate.Format {
            year(Padding.ZERO)
            char('年')
            monthNumber(Padding.ZERO)
            char('月')
            day(Padding.ZERO)
        }
        return LocalDate.parse(startTimeStr, format)
    }

    suspend fun getStartDate(termId: String = ""): LocalDate?{
        val html = getScheduleHtml(termId)
        html?.let {
            return parseStartDate(html)
        }
        return null
    }

    fun parseWeekNum(html: String): Int{
        val doc = Ksoup.parse(html)
        val trElements = doc.select("#kbtable tbody tr")
        return if(trElements.size <= 2) -1
        else trElements.size - 2
    }

    suspend fun getWeekNum(termId: String = ""): Int?{
        val html = getScheduleHtml(termId)
        html?.let {
            return parseWeekNum(html)
        }
        return null
    }
}