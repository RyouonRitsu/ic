package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotEmpty

/**
 * @author ryouonritsu
 */
@Schema(description = "批量ID请求")
data class BulkIdRequest(
    @field:NotEmpty
    @Schema(description = "ID集合", required = true)
    val ids: List<Long>?
)
