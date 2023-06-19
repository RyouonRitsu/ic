package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @Author Kude
 * @Date 2023/6/19 09:30
 */
@Schema(description = "User's information for MRO")
data class MROUserInfoDTO(
    @Schema(description = "用户ID", example = "1", required = true)
    var id: String = "0",
    @Schema(description = "电子邮箱", example = "email@example.com", required = true)
    var email: String,
    @Schema(description = "用户名", example = "username", required = true)
    var username: String,
    @Schema(description = "头像地址", example = "./", required = true)
    var avatar: String = "",
    @Schema(description = "真实姓名", example = "name", required = true)
    var legalName: String = "",
    @Schema(description = "性别", example = "男", required = false)
    var gender: String = "保密",
    @Schema(description = "联系人名", example = "name", required = true)
    var contactName: String = "",
    @Schema(description = "联系方式", example = "12345678901", required = true)
    var phone: String = "",
    @Schema(description = "公司名", example = "BD", required = true)
    var companyName: String = "",
    @Schema(description = "职位", required = true)
    var position: String = "",
    @Schema(description = "用户类型", example = "客户", required = true)
    var userType: String,
)