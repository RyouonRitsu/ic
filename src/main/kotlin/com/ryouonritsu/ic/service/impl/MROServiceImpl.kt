package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.domain.dto.MRODTO
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.repository.MRORepository
import com.ryouonritsu.ic.service.MROService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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

    override fun selectMROByUserId(userId: Long): Response<List<MRODTO>> {
        val orders = mroRepository.findByUserId(userId)
        return Response.success(orders.map { it.toDTO() })
    }
}