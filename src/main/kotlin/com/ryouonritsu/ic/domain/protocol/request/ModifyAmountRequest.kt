package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "编辑购物车请求")
data class ModifyAmountRequest(
    @field:NotNull
    @Schema(description = "记录ID", required = true)
    val recordId: Long?,
    @field:NotNull
    @field:Min(1)
    @Schema(description = "变更后的值", required = true)
    val value: Long?
)
