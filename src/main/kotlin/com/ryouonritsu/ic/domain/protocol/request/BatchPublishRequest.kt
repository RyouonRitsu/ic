package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "批量发布事件请求")
data class BatchPublishRequest(
    @field:NotNull
    @field:NotEmpty
    @Schema(description = "用户ID序列", required = true)
    val userIds: List<Long>?,
    @Schema(description = "名称", example = "log")
    val name: String?,
    @field:NotBlank
    @Schema(description = "消息", example = "Something wrong", required = true)
    val message: String?,
)
