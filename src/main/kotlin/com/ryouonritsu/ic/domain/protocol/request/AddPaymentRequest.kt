package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@Schema(description = "创建缴费单请求")
data class AddPaymentRequest(
    @field:NotNull
    @Schema(description = "租户ID", example = "1", required = true)
    val userId: Long?,
    @field:NotNull
    @Schema(description = "租赁信息ID", example = "1", required = true)
    val rentalId: Long?,
    @field:NotNull
    @field:Min(1)
    @Schema(description = "缴纳金额", example = "0.0", required = true)
    val amount: BigDecimal?
)