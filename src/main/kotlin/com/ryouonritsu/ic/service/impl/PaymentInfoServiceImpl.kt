package com.ryouonritsu.ic.service.impl

import com.alibaba.fastjson2.parseArray
import com.alibaba.fastjson2.toJSONString
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.dto.PaymentInfoDTO
import com.ryouonritsu.ic.domain.protocol.request.AddPaymentRequest
import com.ryouonritsu.ic.domain.protocol.response.ListResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.PaymentInfo
import com.ryouonritsu.ic.repository.PaymentInfoRepository
import com.ryouonritsu.ic.repository.RentalInfoRepository
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.PaymentInfoService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate
import kotlin.jvm.optionals.getOrElse

/**
 * @author ryouonritsu
 */
@Service
class PaymentInfoServiceImpl(
    private val paymentInfoRepository: PaymentInfoRepository,
    private val userRepository: UserRepository,
    private val rentalInfoRepository: RentalInfoRepository,
    private val transactionTemplate: TransactionTemplate
) : PaymentInfoService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun create(request: AddPaymentRequest): Response<PaymentInfoDTO> {
        val user = userRepository.findByIdAndStatus(request.userId!!)
            ?: throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        val rentalInfo = rentalInfoRepository.findById(request.rentalId!!).getOrElse {
            throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        }
        var paymentInfo = PaymentInfo(
            userId = request.userId,
            rentalId = request.rentalId,
            roomId = rentalInfo.roomId,
            amount = request.amount!!
        )
        transactionTemplate.execute {
            paymentInfo = paymentInfoRepository.save(paymentInfo)
            user.paymentInfoIds = user.paymentInfoIds.parseArray<Long>()
                .apply { this += paymentInfo.id }
                .toJSONString()
            userRepository.save(user)
        }
        return Response.success(paymentInfo.toDTO())
    }

    override fun list(
        ids: List<Long>?,
        userId: Long?,
        rentalId: Long?,
        roomId: Long?,
        page: Int,
        limit: Int
    ): Response<ListResponse<PaymentInfoDTO>> {
        val specification = Specification<PaymentInfo> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!ids.isNullOrEmpty()) predicates += cb.`in`(root.get<Long>("id")).apply {
                ids.forEach { this.value(it) }
            }
            if (userId != null) predicates += cb.equal(root.get<Long>("userId"), userId)
            if (rentalId != null) predicates += cb.equal(root.get<Long>("rentalId"), rentalId)
            if (roomId != null) predicates += cb.equal(root.get<Long>("roomId"), roomId)
            predicates += cb.equal(root.get<Boolean>("status"), true)
            query.where(*predicates.toTypedArray())
                .orderBy(cb.asc(root.get<LocalDateTime>("createTime")))
                .restriction
        }
        val result = paymentInfoRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val list = result.content.map { it.toDTO() }
        return Response.success(ListResponse(list, total))
    }

    override fun queryStatusByUserId(userId: Long?): Response<Map<String, Any>> {
        val user = if (userId == null) RequestContext.user!!
        else userRepository.findByIdAndStatus(userId)
            ?: throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        val rentalInfoIds = user.rentalInfoIds.parseArray<Long>()
        val paymentInfoIds = user.paymentInfoIds.parseArray<Long>()
        if (rentalInfoIds.isNullOrEmpty())
            return Response.success(mapOf())

        val result = mutableMapOf<String, Any>()
        rentalInfoIds.forEach { rentalInfoId ->
            val rentalInfo = rentalInfoRepository.findById(rentalInfoId).getOrElse {
                log.error("[PaymentServiceImpl.queryStatusByUserId] can not find rentalInfo by id = $rentalInfoId")
                return@forEach
            }
            val yearRange = rentalInfo.startTime.year..rentalInfo.endTime.year
            log.info("[PaymentServiceImpl.queryStatusByUserId] for $rentalInfoId, yearRange is $yearRange")

            val re = mutableMapOf<Int, Any>()

            fun putDefaultResult() {
                result["$rentalInfoId"] = re.apply {
                    yearRange.forEach { this[it] = mapOf("paymentStatus" to false) }
                }
            }

            if (paymentInfoIds.isNullOrEmpty()) {
                putDefaultResult()
                return@forEach
            }

            val paymentInfoList = paymentInfoRepository.findAllById(paymentInfoIds)
            if (paymentInfoList.isEmpty()) {
                putDefaultResult()
                return@forEach
            }

            val year2PaymentInfo = paymentInfoList
                .filter { it.rentalId == rentalInfoId }
                .groupBy { it.createTime.year }
            for (i in yearRange) {
                if (i in year2PaymentInfo) {
                    re[i] = mapOf(
                        "paymentStatus" to true,
                        "paymentInfoList" to year2PaymentInfo[i]?.map { it.toDTO() }
                    )
                } else re[i] = mapOf("paymentStatus" to false)
            }
            result["$rentalInfoId"] = re
        }

        return Response.success(result)
    }
}