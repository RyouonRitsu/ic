package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Order
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author ryouonritsu
 */
@Repository
interface OrderRepository : JpaRepositoryImplementation<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.id = ?1 AND o.status = true")
    override fun findById(id: Long): Optional<Order>

    @Query("SELECT o FROM Order o WHERE o.id IN ?1 AND o.status = true")
    override fun findAllById(ids: MutableIterable<Long>): MutableList<Order>
}