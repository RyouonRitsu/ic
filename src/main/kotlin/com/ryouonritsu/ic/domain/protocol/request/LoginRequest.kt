package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "用户登录请求")
data class LoginRequest(
    @field:NotBlank
    @Schema(description = "用户名或邮箱", required = true)
    val identifier: String?,
    @field:NotBlank
    @Schema(description = "密码", example = "12345678@", required = true)
    val password: String?,
    @Schema(description = "是否记住登录", example = "true", defaultValue = "false")
    val keepLogin: Boolean = false
)
