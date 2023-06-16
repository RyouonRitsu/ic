package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.RoomFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * @author PaulManstein
 */
@Repository
interface RoomFileRepository : JpaRepository<RoomFile, Long> {
    fun findByUrl(url: String): RoomFile?
}