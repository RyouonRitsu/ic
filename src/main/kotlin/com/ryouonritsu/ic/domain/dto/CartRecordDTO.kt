package com.ryouonritsu.ic.domain.dto

import com.ryouonritsu.ic.common.constants.ICConstant.STR_0
import com.ryouonritsu.ic.entity.CartRecord
import com.ryouonritsu.ic.entity.Goods
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * @author ryouonritsu
 */
@Schema(description = "购物车记录")
data class CartRecordDTO(
    @Schema(description = "ID", required = true)
    var id: String = STR_0,
    @Schema(description = "用户ID", required = true)
    var userId: String = STR_0,
    @Schema(description = "商品信息", required = true)
    var goods: GoodsDTO,
    @Schema(description = "数量", required = true)
    var amount: String = STR_0,
    @Schema(description = "创建时间", required = true)
    var createTime: LocalDateTime = LocalDateTime.now(),
    @Schema(description = "修改时间", required = true)
    var modifyTime: LocalDateTime = LocalDateTime.now(),
) {
    constructor(cartRecord: CartRecord, goods: Goods) : this(
        "${cartRecord.id}",
        "${cartRecord.userId}",
        goods.toDTO(),
        "${cartRecord.amount}",
        cartRecord.createTime,
        cartRecord.modifyTime
    )
}
