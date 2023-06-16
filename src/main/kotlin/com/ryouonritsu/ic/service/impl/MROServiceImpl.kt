package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.domain.protocol.response.ListMROResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.MRO
import com.ryouonritsu.ic.repository.MRORepository
import com.ryouonritsu.ic.service.MROService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import javax.persistence.criteria.Predicate

/**
 * @Author Kude
 * @Date 2023/6/16 14:32
 */
@Service
class MROServiceImpl(
    private val redisUtils: RedisUtils,
    private val mroRepository: MRORepository,
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
        val users = result.content.map { it.toDTO() }
        return Response.success(ListMROResponse(total, users))
    }
}