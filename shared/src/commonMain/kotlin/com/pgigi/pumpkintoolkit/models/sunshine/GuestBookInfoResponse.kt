package com.pgigi.pumpkintoolkit.models.sunshine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 详情查询响应（对应包含 gbInfo、reply、evaluate 的 JSON）
 */
@Serializable
data class GuestBookInfoResponse(
    @SerialName("gbInfo") val gbInfo: GuestBookItem,
    @SerialName("reply") val reply: ReplyDetail? = null,
    @SerialName("evaluate") val evaluate: Evaluate? = null
)

/**
 * 详情接口中的回复对象（比列表中的回复字段更详细）
 */
@Serializable
data class ReplyDetail(
    @SerialName("id") val id: String,
    @SerialName("guestbookid") val guestBookId: String, // 关联的工单ID
    @SerialName("dealcontent") val dealContent: String, // HTML处理详情
    @SerialName("dealdepartmentid") val dealDepartmentId: String,
    @SerialName("dealnodeid") val dealNodeId: String = "", // 处理节点ID
    @SerialName("dealnodename") val dealNodeName: String, // 如 "答复"
    @SerialName("dealtime") val dealTime: Long, // 处理时间戳
    @SerialName("dealuserid") val dealUserId: String, // 处理人ID
    @SerialName("file") val file: String? = null, // 回复附件路径
    @SerialName("filename") val fileName: String? = null, // 回复附件名
    @SerialName("pkname") val pkName: String = ""
)

/**
 * 评价信息（通常为空或仅含 pkname）
 */
@Serializable
data class Evaluate(
    @SerialName("pkname") val pkName: String = ""
    // 扩展：如后续增加评分、评价内容等字段，在此添加
    // @SerialName("score") val score: Int? = null,
    // @SerialName("content") val content: String? = null
)
