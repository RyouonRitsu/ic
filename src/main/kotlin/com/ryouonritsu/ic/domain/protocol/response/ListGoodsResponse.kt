package com.ryouonritsu.ic.domain.protocol.response

import com.ryouonritsu.ic.domain.dto.GoodsDTO
import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "商品查询响应")
data class ListGoodsResponse(
    @Schema(description = "商品列表", required = true)
    val list: List<GoodsDTO>,
    @Schema(description = "总数", required = true)
    val total: Long
)
