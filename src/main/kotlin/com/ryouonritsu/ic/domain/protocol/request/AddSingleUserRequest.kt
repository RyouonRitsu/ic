package com.ryouonritsu.ic.domain.protocol.request

import com.ryouonritsu.ic.common.constants.ICConstant
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

/**
 * @author PaulManstein
 */
@Schema(description = "添加单个用户")
data class AddSingleUserRequest(
    @field:NotBlank
    @field:Email
    @Schema(description = "用户邮箱", example = "1780645196@qq.com", required = true)
    val email: String?,
    @field:NotBlank
    @Schema(description = "用户名", example = "AAA", required = true)
    val username: String?,
    @field:NotBlank
    @Schema(description = "密码", example = "12345678@", required = true)
    val password: String?,
    @field:NotBlank
    @Schema(description = "法人名", example = "AAA", required = true)
    val legalName: String?,
    @field:NotBlank
    @Schema(description = "联系人名", example = "AAA", required = true)
    val contactName: String?,
    @field:NotBlank
    @Schema(description = "联系方式", example = "123456", required = true)
    val phone: String?,
    @Schema(description = "公司名", example = "BUAA")
    val companyName: String?,
    @Schema(
        description = "用户类型",
        example = "0: 客户, 1: 管理员, 2: 水维修人员, 3: 电维修人员, 4: 机器维修人"
    )
    val userType: Int = ICConstant.INT_0,
    @Schema(description = "个人头像", example = "https://128.0.0.1/img/bd_logo1.png")
    val avatar: String?,
)