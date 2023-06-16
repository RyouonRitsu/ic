package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Room
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author PaulManstein
 */

@Repository
interface RoomRepository : JpaRepositoryImplementation<Room, Long> {
    @Query("SELECT r FROM Room r WHERE r.id = ?1") //and isDeleted?
    fun findByRoomId(id: String): Room?

}