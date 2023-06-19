package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Payment
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository


/**
 * @author PaulManstein
 */
@Repository
interface PaymentRepository : JpaRepositoryImplementation<Payment, Long> {

}