package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@Schema
data class AddSingleUserRequest(
    @field:NotBlank
    @field:Email
    @Schema(description = "用户邮箱", example = "1780645196@qq.com", required = true)
    var email: String?,
    @field:NotBlank
    @Schema(description = "用户名", example = "AAA", required = true)
    val username: String?,
    @field:NotBlank
    @Schema(description = "密码", example = "12345678@", required = true)
    val password: String?,
    @Schema(description = "个人头像", example = "https://128.0.0.1/img/bd_logo1.png")
    val avatar: String = "",
    @field:NotBlank
    @Schema(description = "真实姓名", example = "ABC", required = true)
    val legalName: String?
)