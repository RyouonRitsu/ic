package com.ryouonritsu.ic.domain.dto

import com.ryouonritsu.ic.common.constants.ICConstant.EMPTY_STR
import com.ryouonritsu.ic.common.constants.ICConstant.STR_0
import com.ryouonritsu.ic.entity.Goods
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * @author ryouonritsu
 */
@Schema(description = "商品")
data class GoodsDTO(
    @Schema(description = "ID", required = true)
    var id: String = STR_0,
    @Schema(description = "商品名", required = true)
    var name: String = EMPTY_STR,
    @Schema(description = "描述图片地址", required = true)
    var picture: String = EMPTY_STR,
    @Schema(description = "商品类型", required = true)
    var type: String = EMPTY_STR,
    @Schema(description = "商品数量", required = true)
    var amount: String = STR_0,
    @Schema(description = "商品状态", required = true)
    var state: String = Goods.State.UNDER_REVIEW(),
    @Schema(description = "原价", required = true)
    var originalPrice: String = BigDecimal.ZERO.toString(),
    @Schema(description = "折扣比率", required = true)
    var discount: String = BigDecimal.ONE.toString(),
    @Schema(description = "价格", required = true)
    var price: String = BigDecimal.ZERO.toString(),
    @Schema(description = "商品描述", required = true)
    var description: String = EMPTY_STR,
    @Schema(description = "查询计数", required = true)
    var viewCnt: String = STR_0,
    @Schema(description = "销量", required = true)
    var sales: String = STR_0,
    @Schema(description = "创建时间", required = true)
    var createTime: LocalDateTime = LocalDateTime.now(),
    @Schema(description = "修改时间", required = true)
    var modifyTime: LocalDateTime = LocalDateTime.now(),
)
