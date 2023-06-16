package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "修改用户信息请求")
data class ModifyUserInfoRequest(
    @Schema(description = "用户Id，管理员可用")
    var id: Long?,
    @Schema(description = "邮箱，管理员可用")
    var email: String?,
    @Schema(description = "用户名")
    val username: String?,
    @Schema(description = "个人头像")
    val avatar: String?,
    @Schema(description = "真实姓名")
    val legalName: String?,
    @Schema(description = "性别", example = "保密/男/女")
    val gender: String?,
    @Schema(description = "生日", example = "2000-01-01")
    val birthday: String?,
    @Schema(description = "联系人名")
    val contactName: String?,
    @Schema(description = "联系方式", example = "12345678901")
    val phone: String?,
    @Schema(description = "所在地", example = "China")
    val location: String?,
    @Schema(description = "公司名")
    val companyName: String?,
    @Schema(description = "职位")
    val position: String?,
    @Schema(description = "生效状态，管理员可用")
    var status: Boolean?,
)
