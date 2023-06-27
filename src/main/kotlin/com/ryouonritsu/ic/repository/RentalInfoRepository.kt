package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.RentalInfo
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author ryouonritsu
 */
@Repository
interface RentalInfoRepository : JpaRepositoryImplementation<RentalInfo, Long> {
    @Query("SELECT r FROM RentalInfo r WHERE r.id IN ?1 AND r.status = ?2")
    fun findAllByIdsAndStatus(ids: List<Long>, status: Boolean = true): List<RentalInfo>
    fun findAllByStatus(status: Boolean = true): List<RentalInfo>
    fun findByIdAndStatus(id: Long, status: Boolean = true): RentalInfo?
    fun findAllByUserIdAndStatus(userId: Long, status: Boolean = true): List<RentalInfo>
}