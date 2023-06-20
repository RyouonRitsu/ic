package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * @author ryouonritsu
 */
@Schema(description = "Payment entity")
class PaymentInfoDTO(
    @Schema(description = "缴费信息ID", example = "1", required = true)
    val id: String = "0",
    @Schema(description = "租户ID", example = "1", required = true)
    var userId: String = "0",
    @Schema(description = "租赁合同ID", example = "1", required = true)
    var rentalId: String = "0",
    @Schema(description = "房间ID", example = "1", required = true)
    var roomId: String = "0",
    @Schema(description = "缴费时间", required = true)
    var paymentTime: LocalDateTime,
    @Schema(description = "缴纳金额", example = "0.0", required = true)
    var amount: String = "0.0"
)