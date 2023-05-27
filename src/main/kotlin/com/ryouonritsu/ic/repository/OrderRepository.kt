package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Order
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author ryouonritsu
 */
@Repository
interface OrderRepository : JpaRepositoryImplementation<Order, Long> {
}