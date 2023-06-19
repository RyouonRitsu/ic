package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Schema(description = "创建租赁信息")
data class CreateRentalInfoRequest(
    @field:NotNull
    @Schema(description = "房间id", required = true)
    val roomId: Long?,
    @field:NotNull
    @Schema(description = "租赁开始时间，如2019-01-01", required = true)
    val startTime: LocalDate?,
    @field:NotNull
    @Schema(description = "租赁结束时间，如2024-09-03", required = true)
    val endTime: LocalDate?,
    @field:NotNull
    @Schema(description = "租赁总费用", required = true)
    val totalCost: BigDecimal?
)