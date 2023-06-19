package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.MRO
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @Author Kude
 * @Date 2023/6/16 14:27
 */

@Repository
interface MRORepository : JpaRepositoryImplementation<MRO, Long> {
    fun findByIdAndStatus(id: Long, status: Boolean = true): MRO?
}