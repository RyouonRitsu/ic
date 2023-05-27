package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotEmpty

/**
 * @author ryouonritsu
 */
@Schema(description = "从购物车删除请求")
data class RemoveFromCartRequest(
    @field:NotEmpty
    @Schema(description = "记录ID集合", required = true)
    val recordIds: List<Long>?
)
