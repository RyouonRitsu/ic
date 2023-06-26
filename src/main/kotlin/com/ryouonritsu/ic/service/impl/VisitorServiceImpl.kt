package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.dto.VisitorDTO
import com.ryouonritsu.ic.domain.protocol.request.CreateVisitorRequest
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.Visitor
import com.ryouonritsu.ic.manager.db.NotificationManager
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.repository.VisitorRepository
import com.ryouonritsu.ic.service.VisitorService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * @Author Kude
 * @Date 2023/6/26 11:15
 */
@Service
class VisitorServiceImpl(
    private val redisUtils: RedisUtils,
    private val visitorRepository: VisitorRepository,
    private val userRepository: UserRepository,
    private val notificationManager: NotificationManager
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
}