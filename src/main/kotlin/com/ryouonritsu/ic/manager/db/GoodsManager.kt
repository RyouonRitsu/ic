package com.ryouonritsu.ic.manager.db

import com.ryouonritsu.ic.entity.Goods

/**
 * @author ryouonritsu
 */
interface GoodsManager {
    fun adjustProperties(goodsId: Long, amountValue: Long = 0, salesValue: Long = 0)
    fun adjustProperties(goods: Goods, amountValue: Long = 0, salesValue: Long = 0)
}