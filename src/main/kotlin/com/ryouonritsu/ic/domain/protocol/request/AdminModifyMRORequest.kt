package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * @Author Kude
 * @Date 2023/6/18 11:00
 */
@Schema(description = "管理员修改维修工单")
data class AdminModifyMRORequest(
    @field:NotNull
    @field:NotBlank
    @Schema(description = "工单ID", required = true)
    var id: String?,
    @field:NotNull
    @field:NotBlank
    @Schema(description = "维修人员ID", required = true)
    var workerId: String?,
    @Schema(description = "实际维修日期", required = false)
    var actualDate: String?,
    @Schema(description = "实际维修时间段", required = true)
    var actualTime: String?,
)