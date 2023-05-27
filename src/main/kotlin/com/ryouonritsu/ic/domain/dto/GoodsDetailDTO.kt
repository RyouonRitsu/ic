package com.ryouonritsu.ic.domain.dto

import com.ryouonritsu.ic.entity.Goods
import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "订单商品详细信息")
data class GoodsDetailDTO(
    @Schema(description = "商品", required = true)
    val goods: GoodsDTO,
    @Schema(description = "数量", required = true)
    val amount: String
) {
    constructor(goodsInfo: GoodsInfoDTO, goods: Goods) : this(
        goods.toDTO(),
        "${goodsInfo.amount}"
    )
}
