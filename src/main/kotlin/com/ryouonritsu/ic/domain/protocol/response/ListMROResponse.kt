package com.ryouonritsu.ic.domain.protocol.response

import com.ryouonritsu.ic.domain.dto.MRODTO
import io.swagger.v3.oas.annotations.media.Schema

/**
 * @Author Kude
 * @Date 2023/6/16 16:51
 */
@Schema(description = "维修工单查询响应")
data class ListMROResponse(
    @Schema(description = "总数", required = true)
    val total: Long,
    @Schema(description = "列表", required = true)
    val list: List<MRODTO>,
)