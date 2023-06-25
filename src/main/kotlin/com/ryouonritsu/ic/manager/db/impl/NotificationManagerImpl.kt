package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.constants.ICConstant
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.domain.protocol.request.BatchPublishRequest
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.entity.Event
import com.ryouonritsu.ic.manager.db.NotificationManager
import com.ryouonritsu.ic.manager.db.UserManager
import com.ryouonritsu.ic.repository.EventRepository
import com.ryouonritsu.ic.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * @author ryouonritsu
 */
@Component
class NotificationManagerImpl(
    private val eventRepository: EventRepository,
    private val userManager: UserManager,
    private val userRepository: UserRepository,
    private val transactionTemplate: TransactionTemplate,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor
) : NotificationManager {
    companion object {
        private val log = LoggerFactory.getLogger(NotificationManagerImpl::class.java)
    }

    @ServiceLog(description = "发布通知")
    override fun publish(request: PublishRequest): Event {
        userRepository.findByIdAndStatus(
            request.userId ?: throw ServiceException(ExceptionEnum.BAD_REQUEST)
        ) ?: throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)

        var event = Event.from(request)
        transactionTemplate.execute {
            event = eventRepository.save(event)
        }
        asyncSendNotification(listOf(request))

        return event
    }

    @ServiceLog(description = "批量发布通知")
    override fun batchPublish(requests: List<PublishRequest>): List<Event> {
        var events = requests.map {
            userRepository.findByIdAndStatus(
                it.userId ?: throw ServiceException(ExceptionEnum.BAD_REQUEST)
            ) ?: throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)

            Event.from(it)
        }
        transactionTemplate.execute {
            events = eventRepository.saveAll(events)
        }
        asyncSendNotification(requests)

        return events
    }

    @ServiceLog(description = "批量发布通知")
    override fun batchPublish(request: BatchPublishRequest): List<Event> {
        val requests = request.userIds
            ?.map { PublishRequest(it, request.name, request.message, request.needSendEmail) }
            ?: throw ServiceException(ExceptionEnum.BAD_REQUEST)
        return batchPublish(requests)
    }

    private fun asyncSendNotification(requests: List<PublishRequest>) {
        threadPoolTaskExecutor.submit {
            if (requests.isEmpty()) return@submit

            val userIds = requests.map { it.userId!! }
            val users = userRepository.findAllById(userIds)
                .associateBy { it.id }
            if (users.isEmpty()) {
                log.warn("[NotificationManagerImpl.asyncSendNotification] can not find $userIds in database")
                return@submit
            }

            requests.forEach {
                if (!it.needSendEmail) return@forEach

                val f = threadPoolTaskExecutor.submitListenable {
                    val user = users[it.userId]
                        ?: throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
                    val result = userManager.retrySendEmail(
                        user.email,
                        it.name ?: ICConstant.EVENT,
                        it.message!!
                    )
                    if (!result) throw ServiceException(ExceptionEnum.REQUEST_TIMEOUT)
                }
                f.addCallback(
                    { log.info("[NotificationManagerImpl.asyncSendNotification] send notification successful!") },
                    { e ->
                        log.error(
                            "[NotificationManagerImpl.asyncSendNotification] send notification failed!",
                            e
                        )
                    }
                )
            }
        }
    }
}