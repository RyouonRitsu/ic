package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

/**
 * @author ryouonritsu
 */
@Schema(description = "批量下单请求")
data class BulkOrderRequest(
    @field:NotEmpty
    @Schema(description = "记录ID集合", required = true)
    val recordIds: List<Long>?,
    @field:NotBlank
    @Schema(description = "收货地址", required = true)
    val address: String?
)
