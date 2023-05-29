package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.protocol.response.ListCartResponse
import com.ryouonritsu.ic.domain.protocol.response.ListOrderResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.Order
import java.math.BigDecimal

/**
 * @author ryouonritsu
 */
interface ShoppingService {
    fun listCart(
        page: Int,
        limit: Int
    ): Response<ListCartResponse>

    fun addToCart(goodsId: Long, amount: Long): Response<Unit>
    fun modifyAmount(recordId: Long, value: Long): Response<Unit>
    fun removeFromCart(recordIds: List<Long>): Response<Unit>
    fun listOrders(
        keyword: String?,
        states: List<Order.State>?,
        page: Int,
        limit: Int
    ): Response<ListOrderResponse>

    fun bulkOrder(recordIds: List<Long>, address: String): Response<Unit>
    fun order(goodsId: Long, amount: Long, address: String): Response<Unit>
    fun pay(orderId: Long): Response<Unit>
    fun recharge(value: BigDecimal): Response<Unit>
}