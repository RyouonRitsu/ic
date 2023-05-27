package com.ryouonritsu.ic.domain.protocol.response

import com.ryouonritsu.ic.domain.dto.OrderDTO
import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "订单查询响应")
data class ListOrderResponse(
    @Schema(description = "列表", required = true)
    val list: List<OrderDTO>,
    @Schema(description = "总数", required = true)
    val total: Long
)
