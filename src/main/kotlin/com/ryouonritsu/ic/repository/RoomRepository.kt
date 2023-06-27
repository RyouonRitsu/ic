package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Room
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author PaulManstein
 */

@Repository
interface RoomRepository : JpaRepositoryImplementation<Room, Long> {
    fun findAllByUserId(userId: Long): List<Room>
}