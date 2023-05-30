package com.ryouonritsu.ic.domain.dto

import com.ryouonritsu.ic.common.constants.ICConstant.EMPTY_STR
import com.ryouonritsu.ic.common.constants.ICConstant.STR_0
import com.ryouonritsu.ic.entity.Order
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * @author ryouonritsu
 */
@Schema(description = "订单")
data class OrderDTO(
    @Schema(description = "ID", required = true)
    var id: String = STR_0,
    @Schema(description = "用户ID", required = true)
    var userId: String = STR_0,
    @Schema(description = "商品信息", required = true)
    var goodsInfo: List<GoodsDetailDTO>,
    @Schema(description = "收货地址", required = true)
    var address: String = EMPTY_STR,
    @Schema(description = "价格", required = true)
    var price: String = STR_0,
    @Schema(description = "状态", required = true)
    var state: String = Order.State.UNPAID(),
    @Schema(description = "创建时间", required = true)
    var createTime: LocalDateTime = LocalDateTime.now(),
    @Schema(description = "修改时间", required = true)
    var modifyTime: LocalDateTime = LocalDateTime.now(),
) {
    constructor(order: Order, goodsDetails: List<GoodsDetailDTO>) : this(
        "${order.id}",
        "${order.userId}",
        goodsDetails,
        order.address,
        order.price.toString(),
        Order.State.valueOf(order.state)(),
        order.createTime,
        order.modifyTime
    )

    companion object
}
