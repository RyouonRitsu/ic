package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Visitor
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @Author Kude
 * @Date 2023/6/26 11:26
 */
@Repository
interface VisitorRepository : JpaRepositoryImplementation<Visitor, Long> {
    fun findByIdAndStatus(id: Long, status: Boolean = true): Visitor?
}