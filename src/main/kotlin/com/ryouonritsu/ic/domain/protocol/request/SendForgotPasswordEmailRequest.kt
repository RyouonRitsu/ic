package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "发送找回密码验证码请求")
data class SendForgotPasswordEmailRequest(
    @field:NotBlank
    @field:Email
    @Schema(description = "邮箱", required = true)
    val email: String?
)
