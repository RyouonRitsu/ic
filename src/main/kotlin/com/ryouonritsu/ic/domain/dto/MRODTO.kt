package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @Author Kude
 * @Date 2023/6/16 11:30
 */
@Schema(description = "MRO entity")
data class MRODTO(
    @Schema(description = "维修工单ID", example = "1", required = true)
    var id: String = "",
    @Schema(description = "报修客户ID", example = "1", required = true)
    var customId: String = "",
    @Schema(description = "客户信息", example = "{}", required = true)
    var userInfo: MROUserInfoDTO?,
    @Schema(description = "维修工作人员ID", example = "1", required = true)
    var workerId: String = "",
    @Schema(description = "维修人员信息", example = "{}", required = true)
    var workerInfo: MROUserInfoDTO?,
    @Schema(description = "报修房间ID", example = "1", required = true)
    var roomId: String = "",
    @Schema(description = "问题描述", example = "1", required = true)
    var problem: String = "",
    @Schema(description = "期望维修时间段", example = "1", required = true)
    var expectTime: String = "",
    @Schema(description = "实际维修时间段", example = "1", required = true)
    var actualTime: String = "",
    @Schema(description = "解决方法", example = "1", required = true)
    var resolvent: String = "",
    @Schema(description = "实际维修时间", example = "1", required = true)
    var maintenanceTime: String,
    @Schema(description = "是否解决", example = "0", required = true)
    var isSolved: Boolean,
)