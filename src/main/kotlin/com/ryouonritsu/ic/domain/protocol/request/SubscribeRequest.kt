package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "订阅事件请求")
data class SubscribeRequest(
    @field:NotBlank
    @Schema(description = "订阅标签", example = "error", required = true)
    val tag: String?,
    @field:Email
    @field:NotBlank
    @Schema(description = "订阅邮箱", example = "example@example.com", required = true)
    val email: String?,
)
