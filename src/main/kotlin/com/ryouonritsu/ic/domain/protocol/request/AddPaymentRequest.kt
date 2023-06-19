package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotBlank

@Schema(description = "创建缴费单请求")
data class AddPaymentRequest(
    @field:NotBlank
    @Schema(description = "租户ID", example = "1", required = true)
    var userid: Long?,
    @field:NotBlank
    @Schema(description = "租赁合同ID", example = "1", required = true)
    var rentid: Long?,
    @Schema(description = "房间ID", example = "1" , required = true)
    var roomid: Long?,
    @Schema(description = "缴费时间", example = "1900-01-01", required = true)
    var paytime: LocalDate?,
    @Schema(description = "缴纳金额", example = "0.0", required = true)
    var expense: BigDecimal?
)