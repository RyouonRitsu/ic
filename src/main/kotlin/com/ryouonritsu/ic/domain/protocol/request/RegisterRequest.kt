package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "用户注册请求")
data class RegisterRequest(
    @field:NotBlank
    @field:Email
    @Schema(description = "邮箱", example = "1780645196@qq.com", required = true)
    val email: String?,
    @field:NotBlank
    @Schema(description = "验证码", example = "123456", required = true)
    val verificationCode: String?,
    @field:NotBlank
    @Schema(description = "用户名", example = "Ritsu", required = true)
    val username: String?,
    @field:NotBlank
    @Schema(description = "密码", example = "12345678@", required = true)
    val password1: String?,
    @field:NotBlank
    @Schema(description = "确认密码", example = "12345678@", required = true)
    val password2: String?,
    @Schema(description = "个人头像", example = "https://128.0.0.1/img/bd_logo1.png")
    val avatar: String = "",
    @Schema(description = "真实姓名", example = "Ryouonritsu")
    val realName: String = ""
)
