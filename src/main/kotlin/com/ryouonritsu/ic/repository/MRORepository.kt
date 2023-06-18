package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.MRO
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @Author Kude
 * @Date 2023/6/16 14:27
 */

@Repository
interface MRORepository : JpaRepositoryImplementation<MRO, Long> {
    @Query("SELECT o FROM MRO o WHERE o.id = ?1 AND o.status = true")
    fun findByIdAndStatus(id: Long): MRO?
}