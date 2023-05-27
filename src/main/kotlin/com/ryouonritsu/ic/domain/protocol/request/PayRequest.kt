package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "支付请求")
data class PayRequest(
    @field:NotNull
    @Schema(description = "订单ID", required = true)
    val orderId: Long?
)
