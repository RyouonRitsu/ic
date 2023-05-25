package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "添加收货地址请求")
data class AddAddressRequest(
    @field:NotBlank
    @Schema(description = "收货地址", required = true)
    val address: String?,
)
