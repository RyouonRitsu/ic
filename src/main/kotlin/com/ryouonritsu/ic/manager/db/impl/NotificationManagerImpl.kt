package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.entity.Event
import com.ryouonritsu.ic.manager.db.NotificationManager
import com.ryouonritsu.ic.repository.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * @author ryouonritsu
 */
@Component
class NotificationManagerImpl(
    private val eventRepository: EventRepository,
    private val transactionTemplate: TransactionTemplate
) : NotificationManager {
    companion object {
        private val log = LoggerFactory.getLogger(NotificationManagerImpl::class.java)
    }

    @ServiceLog(description = "发布通知")
    override fun publish(request: PublishRequest): Event {
        var event = Event.from(request)
        transactionTemplate.execute {
            event = eventRepository.save(event)
        }

        return event
    }

    @ServiceLog(description = "批量发布通知")
    override fun batchPublish(requests: List<PublishRequest>): List<Event> {
        var events = requests.map { Event.from(it) }
        transactionTemplate.execute {
            events = eventRepository.saveAll(events)
        }

        return events
    }

    @ServiceLog(description = "批量发布通知")
    override fun batchPublish(userIds: List<Long>, name: String?, message: String): List<Event> {
        val requests = userIds.map { PublishRequest(it, name, message) }
        return batchPublish(requests)
    }
}