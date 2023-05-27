package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "充值请求")
data class RechargeRequest(
    @field:NotNull
    @field:Min(0)
    @Schema(description = "金额", required = true)
    val value: BigDecimal?
)
