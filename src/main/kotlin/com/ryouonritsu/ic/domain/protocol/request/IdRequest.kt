package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "ID请求")
data class IdRequest(
    @field:NotNull
    @Schema(description = "ID", required = true)
    val id: Long?
)
