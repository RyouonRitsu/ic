package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.protocol.request.AdminModifyMRORequest
import com.ryouonritsu.ic.domain.protocol.request.CreateMRORequest
import com.ryouonritsu.ic.domain.protocol.response.ListMROResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.MRO
import com.ryouonritsu.ic.repository.MRORepository
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.MROService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.persistence.criteria.Predicate

/**
 * @Author Kude
 * @Date 2023/6/16 14:32
 */
@Service
class MROServiceImpl(
    private val redisUtils: RedisUtils,
    private val mroRepository: MRORepository,
    private val userRepository: UserRepository,
) : MROService {

    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    override fun list(
        id: String?,
        customId: String?,
        workerId: String?,
        roomId: String?,
        isSolved: Boolean?,
        page: Int,
        limit: Int
    ): Response<ListMROResponse> {
        return runCatching {
            val specification = Specification<MRO> { root, query, cb ->
                val predicates = mutableListOf<Predicate>()
                if (!id.isNullOrBlank()) {
                    predicates += cb.equal(root.get<Long>("id"), id)
                }
                if (!customId.isNullOrBlank()) {
                    predicates += cb.equal(root.get<Long>("customId"), customId)
                }
                if (!workerId.isNullOrBlank()) {
                    predicates += cb.equal(root.get<Long>("workerId"), workerId)
                }
                if (!roomId.isNullOrBlank()) {
                    predicates += cb.equal(root.get<Long>("roomId"), roomId)
                }
                if (isSolved != null) {
                    predicates += cb.equal(root.get<Boolean>("isSolved"), isSolved)
                }
                predicates += cb.equal(root.get<Boolean>("status"), true)
                query.where(*predicates.toTypedArray()).restriction
            }
            val result = mroRepository.findAll(specification, PageRequest.of(page - 1, limit))
            val total = result.totalElements
            val orders = result.content.map { it.toDTO() }
            orders.forEach {
                it.userInfo = userRepository.findById(it.customId.toLong()).get().toDTO()
            }
            Response.success(ListMROResponse(total, orders))
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("创建失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun createMRO(request: CreateMRORequest): Response<Unit> {
        return runCatching {
            val user = userRepository.findById(RequestContext.user!!.id).get()
            mroRepository.save(
                MRO(
                    customId = user.id,
                    problem = request.problem!!,
                    expectTime = request.expectTime!!,
                    roomId = request.roomId!!,
                )
            )
            Response.success<Unit>("创建成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("创建失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun adminModifyMRO(request: AdminModifyMRORequest): Response<Unit> {
        return runCatching {
            userRepository.findById(RequestContext.user!!.id).get()
            val mro = mroRepository.findByIdAndStatus(request.id!!.toLong())
                ?: return Response.failure("维修工单不存在")
            mro.workerId = request.workerId!!.toLong()
            mro.actualTime = request.actualTime!!
            mroRepository.save(mro)
            Response.success<Unit>("修改成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("修改失败, 发生意外错误"))
    }
}