package com.pgigi.pumpkintoolkit.models.sunshine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 列表查询响应（对应包含 list 的 JSON）
 */
@Serializable
data class GuestBookListResponse(
    @SerialName("typecode") val typeCode: String,
    @SerialName("total") val total: Int,
    @SerialName("pageIndex") val pageIndex: String, // JSON中为字符串 "1"
    @SerialName("totalPage") val totalPage: Int,
    @SerialName("list") val list: List<GuestBookItem> = emptyList()
)

/**
 * 留言/工单基础信息（列表项与详情共用）
 * 包含所有可能出现的字段，使用可空类型或默认值兼容不同接口返回
 */
@Serializable
data class GuestBookItem(
    // 核心标识
    @SerialName("id") val id: String,
    @SerialName("outcode") val outCode: String, // 工单号如 BH1774103873077
    @SerialName("visitContentId") val visitContentId: String, // 访客内容ID
    @SerialName("pkname") val pkName: String = "",

    // 内容信息
    @SerialName("title") val title: String,
    @SerialName("content") val content: String, // HTML格式内容
    @SerialName("typecode") val typeCode: String, // "wyjy" 我要建议
    @SerialName("typeName") val typeName: String, // "我要建议"

    // 提交人信息（已脱敏）
    @SerialName("man") val man: String, // 如 "王**"
    @SerialName("fromIdentity") val fromIdentity: String? = null,
    @SerialName("fromCsid") val fromCsId: String,
    @SerialName("fromCsmc") val fromCsMc: String, // 如 "阳光服务受理中心"
    @SerialName("fromAddress") val fromAddress: String? = null, // 地点/地址
    @SerialName("fromQq") val fromQq: String? = null,
    @SerialName("fromtel") val fromTel: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("ip") val ip: String,

    // 时间与状态
    @SerialName("adddate") val addDate: Long, // 提交时间戳（毫秒）
    @SerialName("status") val status: Int = 1, // 状态码
    @SerialName("currentstatus") val currentStatus: String, // 当前状态 "1"待处理 "9"已办结等
    @SerialName("ckcondition") val ckCondition: String, // 查看条件 "0"或"1"
    @SerialName("light") val light: String? = null, // 列表中的颜色标记：green/blue/yellow/red
    @SerialName("isopen") val isOpen: Int = 1, // 1:公开 0:不公开
    @SerialName("isreply") val isReply: Int = 0, // 0:未回复 1:已回复

    // 处理部门信息
    @SerialName("nextdealdepartment") val nextDealDepartment: String, // 部门代码 "302"
    @SerialName("nextdealdepartmentname") val nextDealDepartmentName: String, // "网络信息中心"
    @SerialName("nextdealuser") val nextDealUser: String = "",
    @SerialName("acceptuser") val acceptUser: String? = null,
    @SerialName("acceptdept") val acceptDept: String? = null, // 详情接口有，列表接口可能无

    // 回复信息（列表接口可能直接包含）
    @SerialName("answerUserid") val answerUserId: String? = null, // 回复人ID
    @SerialName("answerTruename") val answerTrueName: String? = null, // 回复人姓名
    @SerialName("replyuser") val replyUser: String? = null, // 回复用户名
    @SerialName("replycontent") val replyContent: String? = null, // HTML回复内容
    @SerialName("replytime") val replyTime: Long? = null, // 回复时间戳

    // 附件信息
    @SerialName("attach") val attach: String? = null, // 附件路径
    @SerialName("attachname") val attachName: String? = null, // 附件显示名称

    // 访问统计
    @SerialName("visitCount") val visitCount: Int = 0,
    @SerialName("visite") val visite: String = "0", // 访问标记

    // 流程信息
    @SerialName("orderId") val orderId: String = "",
    @SerialName("siteid") val siteId: String = "www",
    @SerialName("processingTime") val processingTime: String = "", // 处理耗时（天）
    @SerialName("pushStatus") val pushStatus: String = "0",

    // 评价相关（偶尔出现）
    @SerialName("comments") val comments: String? = null, // 备注/评论
    @SerialName("evalDate") val evalDate: Long? = null,
    @SerialName("evalType") val evalType: String? = null
)
