package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
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
    @Schema(description = "真实姓名", example = "real name", required = true)
    var realName: String = "",
    @Schema(description = "性别", example = "男", required = false)
    var gender: String = "保密",
    @Schema(description = "生日", example = "2000-01-01", required = false)
    var birthday: LocalDate? = null,
    @Schema(description = "联系方式", example = "12345678901", required = false)
    var phone: String = "",
    @Schema(description = "所在地", example = "China", required = false)
    var location: String = "",
    @Schema(description = "学历", example = "PhD", required = true)
    var educationalBackground: String = "",
    @Schema(description = "简介和主要成就", example = "一名学生", required = true)
    var description: String = "",
    @Schema(description = "财产", required = true)
    var property: String = BigDecimal.ZERO.toString(),
    @Schema(description = "收货地址", required = true)
    var shippingAddress: List<String> = mutableListOf(),
    @Schema(description = "工作单位", required = true)
    var company: String = "",
    @Schema(description = "所在行业", required = true)
    var industry: String = "",
    @Schema(description = "职务", required = true)
    var position: String = "",
    @Schema(description = "学号", required = true)
    var studentId: String = "",
    @Schema(description = "入学年份", required = true)
    var admissionYear: String = "",
    @Schema(description = "毕业年份", required = true)
    var graduationYear: String = "",
    @Schema(description = "用户信息", required = true)
    var userInfo: UserInfoDTO,
    @Schema(description = "是否管理员", example = "false", required = true)
    var isAdmin: Boolean = false,
    @Schema(description = "是否已删除", example = "false", required = true)
    var isDeleted: Boolean = false,
    @Schema(description = "registration time", required = true)
    var registrationTime: LocalDateTime = LocalDateTime.now(),
)
