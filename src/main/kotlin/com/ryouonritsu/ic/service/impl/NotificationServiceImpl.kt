package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.constants.ICConstant
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.dto.EventDTO
import com.ryouonritsu.ic.domain.protocol.request.BatchPublishRequest
import com.ryouonritsu.ic.domain.protocol.request.BulkIdRequest
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.domain.protocol.response.ListResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.domain.protocol.response.UnreadNotificationResponse
import com.ryouonritsu.ic.entity.Event
import com.ryouonritsu.ic.manager.db.NotificationManager
import com.ryouonritsu.ic.repository.EventRepository
import com.ryouonritsu.ic.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

/**
 * @author ryouonritsu
 */
@Service
class NotificationServiceImpl(
    private val eventRepository: EventRepository,
    private val notificationManager: NotificationManager,
    private val transactionTemplate: TransactionTemplate
) : NotificationService {
    companion object {
        private val log = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    }

    override fun publish(request: PublishRequest): Response<EventDTO> {
        val event = notificationManager.publish(request)
        return Response.success(event.toDTO())
    }

    override fun batchPublish(request: BatchPublishRequest): Response<List<EventDTO>> {
        val events =
            notificationManager.batchPublish(request)
        return Response.success(events.map { it.toDTO() })
    }

    override fun batchRead(request: BulkIdRequest): Response<Unit> {
        val events = eventRepository.findAllById(request.ids!!)
        if (events.isEmpty()) return Response.success()

        events.forEach { it.status = false }
        transactionTemplate.execute { eventRepository.saveAll(events) }
        return Response.success()
    }

    override fun list(keyword: String?, page: Int, limit: Int): Response<ListResponse<EventDTO>> {
        val specification = Specification<Event> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!keyword.isNullOrBlank()) predicates += cb.or(
                cb.like(root["name"], "%$keyword%"),
                cb.like(root["message"], "%$keyword%")
            )
            predicates += cb.equal(root.get<Long>("userId"), RequestContext.user!!.id)
            query.where(*predicates.toTypedArray())
                .orderBy(cb.desc(root.get<LocalDateTime>("createTime")))
                .restriction
        }
        val result =
            eventRepository.findAll(specification, PageRequest.of(page - ICConstant.INT_1, limit))
        val total = result.totalElements
        val events = result.content.map { it.toDTO() }
        return Response.success(ListResponse(events, total))
    }

    override fun hasUnreadNotifications(): Response<UnreadNotificationResponse> {
        val cnt = eventRepository.countByUserIdAndStatus(RequestContext.user!!.id)
        return if (cnt > ICConstant.INT_0) Response.success(UnreadNotificationResponse(true, cnt))
        else Response.success(UnreadNotificationResponse(false, ICConstant.LONG_0))
    }
}