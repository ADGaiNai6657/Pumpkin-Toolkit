package com.pgigi.pumpkintoolkit.utils

import com.pgigi.pumpkintoolkit.models.sunshine.GuestBookInfoResponse
import com.pgigi.pumpkintoolkit.models.sunshine.GuestBookListResponse
import com.pgigi.pumpkintoolkit.models.sunshine.LostAndFoundResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLPath

object SunshineClient {
    const val TAG = "SunshineClient"
    val client = HttpClient {
        followRedirects = false
    }

    object TypeCode{
        /**
         * 全部
         */
        const val ALL = ""
        /**
         * 政策咨询
         */
        const val POLICY_CONSULTATION = "zczx"
        /**
         * 我要反映
         */
        const val REFLECT = "wyfy"
        /**
         * 我要报修
         */
        const val REPAIR = "wybx"
        /**
         * 求助帮扶
         */
        const val HELP = "qbfz"
        /**
         * 舆情反映
         */
        const val OPINION = "yqfy"
        /**
         * 我要表扬
         */
        const val PRAISE = "wyby"
        /**
         * 我要建议
         */
        const val SUGGEST = "wyjy"
        /**
         * 投诉受理
         */
        const val COMPLAINT = "tssl"
    }

    suspend fun getGuestBookList(pageIndex: Int = 1, pageSize: Int = 20, typeCode: String = TypeCode.ALL, searchKey: String = ""): GuestBookListResponse? {
        try{
            val response = client.post("http://usc.tabbycms.com/ext/GuestbookServletInShtml") {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody(
                    ("pageIndex=${pageIndex}" +
                            "&pageSize=${pageSize}" +
                            "&typecode=${typeCode}" +
                            "&status=1" +
                            "&condition=1" +
                            "&show=2" +
                            "&searchKey=${searchKey}" +
                            "&method=getGuesbBookListInShtml")
                        .encodeURLPath()
                )
            }
            println(TAG+"|"+response.bodyAsText())
            return JsonUtil.parseJson(response.bodyAsText(), GuestBookListResponse.serializer())
        }catch (e: Exception){
            println("$TAG|getGuestBookList: "+e.message)
        }
        return null
    }
    suspend fun getGuestBookInfo(id: String): GuestBookInfoResponse? {
        try{
            val response = client.post("http://usc.tabbycms.com/ext/GuestbookServletInShtml") {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody(
                    ("id=${id}" +
                            "&method=showGuestBookInfoInShtml"
                            )
                        .encodeURLPath()
                )
            }
            return JsonUtil.parseJson(response.bodyAsText(), GuestBookInfoResponse.serializer())
        }catch (e: Exception){
            println("$TAG|getGuestBookInfo: "+e.message)
        }
        return  null
    }
    suspend fun getLostAndFoundList(pageIndex: Int = 1, pageSize: Int = 20, searchKey: String = ""): LostAndFoundResponse? {
        try{
            val response = client.post("http://usc.tabbycms.com/LostPropertyServlet") {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody(("action=getLostPropertyList" +
                        "&pageIndex=${pageIndex}" +
                        "&pageSize=${pageSize}" +
                        "&keyword=${searchKey}"
                        ).encodeURLPath()
                )
            }
            return JsonUtil.parseJson(response.bodyAsText(), LostAndFoundResponse.serializer())
        }catch (e: Exception){
            println("$TAG|getLostAndFoundList: "+e.message)
        }
        return  null
    }
}