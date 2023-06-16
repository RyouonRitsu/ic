package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author ryouonritsu
 */
@Schema(description = "User entity")
data class UserDTO(
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
    @Schema(description = "生日", example = "2000-01-01", required = false)
    var birthday: LocalDate? = null,
    @Schema(description = "联系人名", example = "name", required = true)
    var contactName: String = "",
    @Schema(description = "联系方式", example = "12345678901", required = true)
    var phone: String = "",
    @Schema(description = "所在地", example = "China", required = false)
    var location: String = "",
    @Schema(description = "公司名", example = "BD", required = true)
    var companyName: String = "",
    @Schema(description = "租赁信息id序列", example = "[1,2]", required = true)
    var rentalInfoIds: List<String> = listOf(),
    @Schema(description = "缴纳信息id序列", required = true)
    var paymentInfo: List<String> = listOf(),
    @Schema(description = "职位", required = true)
    var position: String = "",
    @Schema(description = "用户类型", example = "客户", required = true)
    var userType: String,
    @Schema(description = "registration time", required = true)
    var registrationTime: LocalDateTime = LocalDateTime.now(),
)
