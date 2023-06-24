package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * @Author Kude
 * @Date 2023/6/18 09:23
 */
@Schema(description = "创建维修工单")
data class CreateMRORequest(
    @field:NotNull
    @field:NotBlank
    @Schema(description = "问题描述", required = true)
    var problem: String?,
    @Schema(description = "期望时间段", required = false)
    var expectTime: String?,
    @field:NotNull
    @Schema(description = "报修房间号", required = true)
    var roomId: Long?,
    @field:NotNull
    @field:NotBlank
    @Schema(description = "维修类型, example = water, electric, machine", required = true)
    var label: String?,
    @field:NotNull
    @field:NotBlank
    @Schema(description = "实际维修日期", required = true)
    var actualDate: String?,
    @field:NotNull
    @field:NotBlank
    @Schema(description = "实际维修时间段", required = true)
    var actualTime: String?,
)
