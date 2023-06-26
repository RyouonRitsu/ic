package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.dto.VisitorDTO
import com.ryouonritsu.ic.domain.protocol.request.CreateVisitorRequest
import com.ryouonritsu.ic.domain.protocol.response.ListResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.Visitor
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.repository.VisitorRepository
import com.ryouonritsu.ic.service.VisitorService
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.persistence.criteria.Predicate

/**
 * @Author Kude
 * @Date 2023/6/26 11:15
 */
@Service
class VisitorServiceImpl(
    private val redisUtils: RedisUtils,
    private val visitorRepository: VisitorRepository,
    private val userRepository: UserRepository,
) : VisitorService {
    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun createVisitor(request: CreateVisitorRequest): Response<VisitorDTO> {
        val user = userRepository.findByIdAndStatus(RequestContext.user!!.id) ?: run {
            redisUtils - "${RequestContext.user!!.id}"
            return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
        }
        var visitor = Visitor(
            customId = user.id,
            visitorName = request.visitorName!!,
            cardNumber = request.cardNumber!!,
            phoneNumber = request.phoneNumber!!,
            visitTime = request.visitTime!!,
        )
        visitor = visitorRepository.save(visitor)
        return Response.success(visitor.toDTO())
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun statisticsDay(date: String): Response<Map<String, List<Any>>> {
        return runCatching {
            val endDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            val endTime = LocalDateTime.of(endDate, LocalTime.MAX)
            val startTime = endTime.minusDays(19)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
            val specification = Specification<Visitor> { root, query, cb ->
                val predicates = mutableListOf<Predicate>()
                predicates += cb.greaterThanOrEqualTo(root.get("visitTime"), startTime)
                predicates += cb.lessThanOrEqualTo(root.get("visitTime"), endTime)
                query.where(*predicates.toTypedArray()).restriction
            }
            val visitList = visitorRepository.findAll(specification)
                .groupBy { it.visitTime.toLocalDate().toString() }
                .mapValues { it.value.size }
                .toMutableMap()
            for (i in 0 until 21) {
                val iDate = endDate.minusDays(i.toLong()).toString()
                if (!visitList.containsKey(iDate)) {
                    visitList[iDate] = 0
                }
            }
            val visitPair = visitList.toList().sortedBy { it.first }
            val visitMap = mapOf<String, List<Any>>(
                "date" to visitPair.map { it.first },
                "count" to visitPair.map { it.second }
            )
            Response.success("查询成功", visitMap)
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("查询失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun statisticsMonth(year: Int): Response<Map<String, List<Any>>> {
        return runCatching {
            val specification = Specification<Visitor> { root, query, cb ->
                val predicates = mutableListOf<Predicate>()
                predicates += cb.greaterThanOrEqualTo(
                    root.get("visitTime"),
                    LocalDateTime.of(year, 1, 1, 0, 0, 0)
                )
                predicates += cb.lessThanOrEqualTo(
                    root.get("visitTime"),
                    LocalDateTime.of(year, 12, 31, 23, 59, 59)
                )
                predicates += cb.equal(root.get<Boolean>("status"), true)
                query.where(*predicates.toTypedArray()).restriction
            }
            val visitList = visitorRepository.findAll(specification)
                .groupBy { it.visitTime.month.value }
                .mapValues { it.value.size }
                .toMutableMap()
            for (i in 1 until 13) {
                if (!visitList.containsKey(i)) {
                    visitList[i] = 0
                }
            }
            val visitPair = visitList.toList().sortedBy { it.first }
            val visitMap = mapOf<String, List<Any>>(
                "month" to visitPair.map { it.first },
                "count" to visitPair.map { it.second }
            )
            Response.success("查询成功", visitMap)
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("查询失败, 发生意外错误"))
    }

    override fun list(ids: List<Long>?, userId: Long?, page: Int, limit: Int): Response<ListResponse<VisitorDTO>> {
        val specification = Specification<Visitor> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!ids.isNullOrEmpty()) predicates += cb.`in`(root.get<Long>("id")).apply {
                ids.forEach { this.value(it) }
            }
            predicates += cb.equal(root.get<Long>("customId"), userId ?: RequestContext.user!!.id)
            predicates += cb.equal(root.get<Boolean>("status"), true)
            query.where(*predicates.toTypedArray())
                .orderBy(cb.desc(root.get<LocalDateTime>("createTime")))
                .restriction
        }
        val result = visitorRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val list = result.content.map { it.toDTO() }
        return Response.success(ListResponse(list, total))
    }
}