package com.pgigi.pumpkintoolkit.models.sunshine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LostAndFoundResponse(
    val total: Int,
    val list: List<LostItem>
)

@Serializable
data class LostItem(
    /** 物品描述 */
    val description: String = "",

    /** 唯一ID */
    val id: String,

    /** 联系人（可选） */
    @SerialName("linkMan")
    val linkMan: String? = null,

    /** 联系电话（可选） */
    @SerialName("linkPhone")
    val linkPhone: String? = null,

    /** 联系QQ（可选） */
    @SerialName("linkQQ")
    val linkQQ: String? = null,

    /** 丢失地点 */
    @SerialName("lostPlace")
    val lostPlace: String = "",

    /** 丢失时间（时间戳，毫秒） */
    @SerialName("lostTime")
    val lostTime: Long = 0L,

    /** 丢失类型：1-丢失 */
    @SerialName("lostType")
    val lostType: String = "1",

    /** 备用字段（通常为空） */
    val pkname: String = "",

    /** 物品名称 */
    @SerialName("propertyName")
    val propertyName: String = "",

    /** 显示标志：0-显示 */
    @SerialName("showFlag")
    val showFlag: String = "0",

    /** 状态：0-正常 */
    val status: String = "0",

    /** 是否已找到：0-未找到，1-已找到 */
    @SerialName("successFlag")
    val successFlag: String = "0",

    /** 附件路径（可选） */
    val attach: String? = null,

    /** 附件名称（可选） */
    @SerialName("attachname")
    val attachName: String? = null,

    /** 当前存放位置（可选，招领成功后） */
    @SerialName("nowPlace")
    val nowPlace: String? = null
)