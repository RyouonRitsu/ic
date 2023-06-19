package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/**
 * @author PaulManstein
 */
@Schema(description = "Payment entity")
class PaymentDTO(
    @Schema(description = "缴费单ID", example = "1", required = true)
    var id: String = "0",
    @Schema(description = "租户ID", example = "1", required = true)
    var userid: String = "0",
    @Schema(description = "租赁合同ID", example = "1", required = true)
    var rentid: String = "0",
    @Schema(description = "房间ID", example = "1", required = true)
    var roomid: String = "0",
    @Schema(description = "缴费时间", example = "1900-01-01", required = true)
    var paytime: LocalDate? = null,
    @Schema(description = "缴纳金额", example = "0.0", required = true)
    var expense: String = "0.0"
)