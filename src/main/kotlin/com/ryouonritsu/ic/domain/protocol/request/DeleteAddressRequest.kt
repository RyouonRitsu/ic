package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "删除收货地址请求")
data class DeleteAddressRequest(
    @field:NotNull
    @Schema(description = "收货地址索引", required = true)
    val index: Int?,
)
