package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
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
    override fun createVisitor(request: CreateVisitorRequest): Response<Unit> {
        return runCatching {
            val user = userRepository.findByIdAndStatus(RequestContext.user!!.id)
            if (user == null) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            visitorRepository.save(
                Visitor(
                    customId = user.id,
                    visitorName = request.visitorName,
                    cardNumber = request.cardNumber,
                    phoneNumber = request.phoneNumber,
                    visitTime = request.visitTime,
                )
            )
            Response.success<Unit>("创建成功")
        }.onFailure {
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("创建失败, 发生意外错误"))
    }
}