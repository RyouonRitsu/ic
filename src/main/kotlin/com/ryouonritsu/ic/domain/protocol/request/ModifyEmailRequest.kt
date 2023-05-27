package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "修改邮箱请求")
data class ModifyEmailRequest(
    @field:NotBlank
    @field:Email
    @Schema(description = "新邮箱", required = true)
    val email: String?,
    @field:NotBlank
    @Schema(description = "邮箱验证码", required = true)
    val verifyCode: String?,
    @field:NotBlank
    @Schema(description = "密码", required = true)
    val password: String?
)
