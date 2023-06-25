package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.PaymentInfo
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository


/**
 * @author ryouonritsu
 */
@Repository
interface PaymentInfoRepository : JpaRepositoryImplementation<PaymentInfo, Long> {
    @Query("SELECT p FROM PaymentInfo p WHERE p.id IN ?1 AND p.status = ?2")
    fun findAllByIdsAndStatus(ids: List<Long>, status: Boolean = true): List<PaymentInfo>
    fun findAllByRentalIdAndStatus(rentalId: Long, status: Boolean = true): List<PaymentInfo>
}