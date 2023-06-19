package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.time.LocalDate


@Schema(description = "创建租赁信息")
data class CreateRentalInfoRequest (
    @field:NotNull
    @Schema(description="房间id", required = true)
    var roomId: Long?,

    @field:NotNull
    @Schema(description="租赁开始时间", required = true)
    var startTime: LocalDate,

    @field:NotNull
    @Schema(description="租赁结束时间", required = true)
    var endTime: LocalDate,

    @field:NotNull
    @Schema(description="租赁总费用", required = true)
    var totalCost: BigDecimal,


)