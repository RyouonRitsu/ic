package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.CartRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author ryouonritsu
 */
@Repository
interface CartRecordRepository : JpaRepositoryImplementation<CartRecord, Long> {
    @Query("SELECT r FROM CartRecord r WHERE r.userId = ?1 AND r.status = true ORDER BY r.createTime DESC")
    fun findByUserId(
        userId: Long,
        pageable: Pageable = PageRequest.of(0, 10)
    ): Page<CartRecord>

    @Query("SELECT r FROM CartRecord r WHERE r.userId = ?1 AND r.goodsId = ?2 AND r.status = true")
    fun findByUserIdAndGoodsId(userId: Long, goodsId: Long): CartRecord?

    @Query("SELECT r FROM CartRecord r WHERE r.id = ?1 AND r.status = true")
    override fun findById(id: Long): Optional<CartRecord>

    @Query("SELECT r FROM CartRecord r WHERE r.id IN ?1 AND r.status = true")
    override fun findAllById(ids: MutableIterable<Long>): MutableList<CartRecord>
}