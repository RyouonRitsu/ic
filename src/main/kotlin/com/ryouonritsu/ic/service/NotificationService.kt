package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.EventDTO
import com.ryouonritsu.ic.domain.dto.SubscriberDTO
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.domain.protocol.request.SubscribeRequest
import com.ryouonritsu.ic.domain.protocol.request.UnsubscribeRequest
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @author ryouonritsu
 */
interface NotificationService {
    fun publish(request: PublishRequest): Response<EventDTO>
    fun subscribe(request: SubscribeRequest): Response<SubscriberDTO>
    fun unsubscribe(request: UnsubscribeRequest): Response<Unit>
    fun sendVerificationCode(email: String): Response<Unit>
}