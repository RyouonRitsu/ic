package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "用户信息")
data class UserInfoDTO(
    @Schema(description = "身份证号")
    var idNumber: String = "",
)
