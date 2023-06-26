package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Event
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author ryouonritsu
 */
@Repository
interface EventRepository : JpaRepositoryImplementation<Event, Long> {
    fun findAllByUserIdAndStatus(userId: Long, status: Boolean = true): List<Event>
    fun countByUserIdAndStatus(userId: Long, status: Boolean = true): Long
}