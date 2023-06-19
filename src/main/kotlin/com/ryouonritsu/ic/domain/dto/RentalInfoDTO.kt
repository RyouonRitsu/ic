package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "RentalInfo entity")
data class RentalInfoDTO(
    @Schema(description = "租赁信息ID", example = "1", required = true)
    var id: String = "",
    @Schema(description = "客户ID", example = "1", required = true)
    var userId: String = "",
    @Schema(description = "房间ID", example = "1", required = true)
    var roomId: String = "",
    @Schema(description = "开始时间", required = true)
    var startTime: LocalDate,
    @Schema(description = "结束时间", required = true)
    var endTime: LocalDate,
    @Schema(description = "签约时间", required = true)
    var signTime: LocalDateTime,
    @Schema(description = "租赁总费用", example = "1", required = true)
    var totalCost: String = "",
)