package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.EventDTO
import com.ryouonritsu.ic.domain.protocol.request.BatchPublishRequest
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.domain.protocol.response.ListResponse
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @author ryouonritsu
 */
interface NotificationService {
    fun publish(request: PublishRequest): Response<EventDTO>
    fun batchPublish(request: BatchPublishRequest): Response<List<EventDTO>>
    fun list(
        keyword: String?,
        page: Int,
        limit: Int
    ): Response<ListResponse<EventDTO>>

    fun hasUnreadNotifications(): Response<Boolean>
}