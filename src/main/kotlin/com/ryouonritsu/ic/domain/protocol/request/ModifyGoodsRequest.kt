package com.ryouonritsu.ic.domain.protocol.request

import com.ryouonritsu.ic.entity.Goods
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
data class ModifyGoodsRequest(
    @field:NotNull
    @Schema(description = "商品ID", required = true)
    val id: Long,
    @Schema(description = "商品名")
    val name: String?,
    @Schema(description = "描述图片地址")
    val picture: String?,
    @Schema(description = "商品类型")
    val type: String?,
    @Schema(description = "商品数量")
    val amount: Long?,
    @Schema(description = "商品状态")
    val state: Goods.State?,
    @Schema(description = "原价")
    val price: BigDecimal?,
    @Schema(description = "折扣比率")
    val discount: BigDecimal?,
    @Schema(description = "商品描述")
    val description: String?,
)
