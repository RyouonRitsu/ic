package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @Author Kude
 * @Date 2023/6/26 10:34
 */
@Schema(description = "访客附加信息")
data class VisitorInfoDTO(
    @Schema(description = "暂时留空")
    var occupancy: String = "",
)