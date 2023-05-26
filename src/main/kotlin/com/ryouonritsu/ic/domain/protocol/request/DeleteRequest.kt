package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "删除请求")
data class DeleteRequest(
    @field:NotNull
    @Schema(description = "主键ID", required = true)
    val id: Long?
)
