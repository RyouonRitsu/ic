package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "取消订阅请求")
data class UnsubscribeRequest(
    @field:NotBlank
    @Schema(description = "订阅标签", example = "error", required = true)
    val tag: String?,
    @field:Email
    @field:NotBlank
    @Schema(description = "订阅邮箱", example = "example@example.com", required = true)
    val email: String?,
    @field:NotBlank
    @Schema(description = "验证码", example = "123456", required = true)
    val verificationCode: String?
)
