package com.ryouonritsu.ic.domain.protocol.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "列表查询响应")
data class ListResponse<T>(
    @Schema(description = "数据列表", required = true)
    val list: List<T>,
    @Schema(description = "总数", required = true)
    val total: Long
)
