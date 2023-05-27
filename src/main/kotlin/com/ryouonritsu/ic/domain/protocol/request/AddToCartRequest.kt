package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "添加购物车请求")
data class AddToCartRequest(
    @field:NotNull
    @Schema(description = "商品ID", required = true)
    val goodsId: Long?,
    @field:NotNull
    @field:Min(1)
    @Schema(description = "用户输入的数量", required = true)
    val amount: Long?
)
