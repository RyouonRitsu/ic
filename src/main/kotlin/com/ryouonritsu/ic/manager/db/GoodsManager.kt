package com.ryouonritsu.ic.manager.db

/**
 * @author ryouonritsu
 */
interface GoodsManager {
    fun adjustAmount(goodsId: Long, value: Long)
}