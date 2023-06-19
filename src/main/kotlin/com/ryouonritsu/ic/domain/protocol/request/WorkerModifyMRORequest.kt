package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

/**
 * @Author Kude
 * @Date 2023/6/19 09:42
 */
@Schema(description = "维修人员修改维修工单")
data class WorkerModifyMRORequest(
    @field:NotNull
    @Schema(description = "工单ID", required = true)
    var id: String?,
    @field:NotNull
    @Schema(description = "解决办法", required = true)
    var resolvent: String?,
    @field:NotNull
    @Schema(description = "维修时间段", required = true)
    var maintenanceTime: String?,
)