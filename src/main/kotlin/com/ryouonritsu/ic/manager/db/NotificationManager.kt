package com.ryouonritsu.ic.manager.db

import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.entity.Event

/**
 * @author ryouonritsu
 */
interface NotificationManager {
    fun publish(request: PublishRequest): Event
    fun batchPublish(requests: List<PublishRequest>): List<Event>
    fun batchPublish(userIds: List<Long>, name: String?, message: String): List<Event>
}