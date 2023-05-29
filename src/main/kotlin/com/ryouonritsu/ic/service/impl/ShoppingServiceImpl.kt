package com.ryouonritsu.ic.service.impl

import com.alibaba.fastjson2.parseArray
import com.alibaba.fastjson2.toJSONString
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.dto.CartRecordDTO
import com.ryouonritsu.ic.domain.dto.GoodsDetailDTO
import com.ryouonritsu.ic.domain.dto.GoodsInfoDTO
import com.ryouonritsu.ic.domain.dto.OrderDTO
import com.ryouonritsu.ic.domain.protocol.response.ListCartResponse
import com.ryouonritsu.ic.domain.protocol.response.ListOrderResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.CartRecord
import com.ryouonritsu.ic.entity.Order
import com.ryouonritsu.ic.manager.db.GoodsManager
import com.ryouonritsu.ic.manager.db.UserManager
import com.ryouonritsu.ic.repository.CartRecordRepository
import com.ryouonritsu.ic.repository.GoodsRepository
import com.ryouonritsu.ic.repository.OrderRepository
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.ShoppingService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate
import kotlin.jvm.optionals.getOrElse

/**
 * @author ryouonritsu
 */
@Service
class ShoppingServiceImpl(
    private val userManager: UserManager,
    private val userRepository: UserRepository,
    private val goodsManager: GoodsManager,
    private val goodsRepository: GoodsRepository,
    private val cartRecordRepository: CartRecordRepository,
    private val orderRepository: OrderRepository,
    private val transactionTemplate: TransactionTemplate
) : ShoppingService {
    companion object {
        private val log = LoggerFactory.getLogger(ShoppingServiceImpl::class.java)
    }

    override fun listCart(page: Int, limit: Int): Response<ListCartResponse> {
        val result = cartRecordRepository.findByUserId(
            RequestContext.userId.get()!!,
            PageRequest.of(page - 1, limit)
        )
        val total = result.totalElements
        val list = result.content.map {
            val goods = goodsRepository.findById(it.goodsId).getOrElse {
                log.error("[listCart] goods does not exist, userId = ${it.userId}, goodsId = ${it.goodsId}")
                throw ServiceException(ExceptionEnum.NOT_FOUND)
            }
            CartRecordDTO(it, goods)
        }
        return Response.success(ListCartResponse(list, total))
    }

    override fun addToCart(goodsId: Long, amount: Long): Response<Unit> {
        val userId = RequestContext.userId.get()!!
        val record = run {
            val r = cartRecordRepository.findByUserIdAndGoodsId(userId, goodsId)
                ?: return@run CartRecord(
                    userId = userId,
                    goodsId = goodsId,
                    amount = amount
                )
            log.info("[addToCart] $userId already had this goods in the cart")
            r.amount += amount
            r
        }
        transactionTemplate.execute { cartRecordRepository.save(record) }
        return Response.success()
    }

    override fun modifyAmount(recordId: Long, value: Long): Response<Unit> {
        val record = cartRecordRepository.findById(recordId).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        record.amount = value
        transactionTemplate.execute { cartRecordRepository.save(record) }
        return Response.success()
    }

    override fun removeFromCart(recordIds: List<Long>): Response<Unit> {
        val records = cartRecordRepository.findAllById(recordIds as MutableList<Long>)
        if (records.isEmpty()) throw ServiceException(ExceptionEnum.NOT_FOUND)
        records.forEach {
            it.status = false
            transactionTemplate.execute { _ -> cartRecordRepository.save(it) }
        }
        return Response.success()
    }

    override fun listOrders(
        keyword: String?,
        states: List<Order.State>?,
        page: Int,
        limit: Int
    ): Response<ListOrderResponse> {
        val specification = Specification<Order> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!keyword.isNullOrBlank()) predicates += cb.like(root["goodsInfo"], "%$keyword%")
            if (!states.isNullOrEmpty()) predicates += cb.`in`(root.get<Int>("state")).apply {
                states.forEach { this.value(it.code) }
            }
            predicates += cb.equal(root.get<Boolean>("status"), true)
            query.where(*predicates.toTypedArray())
                .orderBy(cb.desc(root.get<LocalDateTime>("createTime")))
                .restriction
        }
        val result = orderRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val list = result.content.map {
            val info = it.goodsInfo.parseArray<GoodsInfoDTO>()
            val details = info.map { i ->
                val goods = goodsRepository.findById(i.id).getOrElse {
                    throw ServiceException(ExceptionEnum.NOT_FOUND)
                }
                GoodsDetailDTO(i, goods)
            }
            OrderDTO(it, details)
        }
        return Response.success(ListOrderResponse(list, total))
    }

    override fun bulkOrder(recordIds: List<Long>, address: String): Response<Unit> {
        val records = cartRecordRepository.findAllById(recordIds as MutableList<Long>)
        if (records.isEmpty()) throw ServiceException(ExceptionEnum.NOT_FOUND)
        val goodsList = goodsRepository.findAllById(records.map { it.goodsId } as MutableList<Long>)
        val goodsId2Records = records.groupBy { it.goodsId }
        val goodsDetails = goodsList.map {
            GoodsDetailDTO(
                it.toDTO(),
                amount = goodsId2Records[it.id]?.sumOf { r -> r.amount }?.toString() ?: run {
                    log.error("[bulkOrder] cannot find goods mapping to record, goodsId = ${it.id}")
                    throw ServiceException(ExceptionEnum.DATA_ERROR)
                }
            )
        }
        val price = goodsDetails.map { it.goods.price.toBigDecimal() * it.amount.toBigDecimal() }
            .sumOf { it }
        val order = Order(
            userId = RequestContext.userId.get()!!,
            goodsInfo = goodsDetails.map { GoodsInfoDTO.from(it) }.toJSONString(),
            address = address,
            price = price
        )
        records.forEach { it.status = false }
        transactionTemplate.execute {
            orderRepository.save(order)
            cartRecordRepository.saveAll(records)
        }
        return Response.success()
    }

    override fun order(goodsId: Long, amount: Long, address: String): Response<Unit> {
        val goods = goodsRepository.findById(goodsId).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        val order = Order(
            userId = RequestContext.userId.get()!!,
            goodsInfo = listOf(GoodsInfoDTO(goodsId, goods.name, amount)).toJSONString(),
            address = address,
            price = goods.price * amount.toBigDecimal()
        )
        transactionTemplate.execute { orderRepository.save(order) }
        return Response.success()
    }

    override fun pay(orderId: Long): Response<Unit> {
        val order = orderRepository.findById(orderId).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        if (order.state != Order.State.UNPAID.code)
            throw ServiceException(ExceptionEnum.DATA_TYPE_IS_INVALID)
        val user = userRepository.findById(RequestContext.userId.get()!!).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        if (user.property < order.price) return Response.failure("余额不足")
        transactionTemplate.execute { _ ->
            userManager.adjustProperty(user, -order.price)
            order.goodsInfo.parseArray<GoodsInfoDTO>().forEach {
                goodsManager.adjustProperties(it.id, -it.amount, it.amount)
            }
            order.state = Order.State.PAID.code
            orderRepository.save(order)
        }
        return Response.success()
    }

    override fun recharge(value: BigDecimal): Response<Unit> {
        userManager.adjustProperty(RequestContext.userId.get()!!, value)
        return Response.success()
    }
}