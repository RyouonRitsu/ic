package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "下单请求")
data class OrderRequest(
    @field:NotNull
    @Schema(description = "商品ID", required = true)
    val goodsId: Long?,
    @field:Min(1)
    @field:NotNull
    @Schema(description = "数量", required = true)
    val amount: Long?,
    @field:NotBlank
    @Schema(description = "收货地址", required = true)
    val address: String?
)
