package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "Payment status")
data class PaymentStatusDTO(
    @Schema(description = "缴费状态", required = true)
    val paymentStatus: Boolean,
    @Schema(description = "缴费信息列表", required = false)
    val paymentInfoList: List<PaymentInfoDTO>? = null
)
