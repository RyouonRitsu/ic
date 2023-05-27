package com.ryouonritsu.ic.domain.dto

/**
 * @author ryouonritsu
 */
data class GoodsInfoDTO(
    val id: Long,
    val name: String,
    val amount: Long
) {
    companion object {
        fun from(detail: GoodsDetailDTO) = GoodsInfoDTO(
            detail.goods.id.toLong(),
            detail.goods.name,
            detail.amount.toLong()
        )
    }
}
