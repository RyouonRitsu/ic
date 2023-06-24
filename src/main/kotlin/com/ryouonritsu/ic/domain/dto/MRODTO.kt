package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

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
    @Schema(description = "实际维修日期", example = "1", required = true)
    var actualDate: String = "",
    @Schema(description = "实际维修时间段", example = "1", required = true)
    var actualTime: String = "",
    @Schema(description = "解决方法", example = "1", required = true)
    var resolvent: String = "",
    @Schema(description = "实际维修时间", example = "1", required = true)
    var maintenanceTime: String,
    @Schema(description = "订单状态", example = "0", required = true)
    var mroStatus: String,
    @Schema(description = "订单创建时间", example = "2023-06-16 18:19:22", required = true)
    var createTime: LocalDateTime,
    @Schema(description = "订单最后修改时间", example = "2023-06-16 18:19:22", required = true)
    var modifyTime: LocalDateTime,
    @Schema(description = "实际维修时间", example = "1", required = true)
    var label: String,
)