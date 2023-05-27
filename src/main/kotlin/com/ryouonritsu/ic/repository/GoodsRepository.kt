package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Goods
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author ryouonritsu
 */
@Repository
interface GoodsRepository : JpaRepositoryImplementation<Goods, Long> {
    @Query("SELECT g FROM Goods g WHERE g.id = ?1 AND g.status = true")
    override fun findById(id: Long): Optional<Goods>

    @Query("SELECT g FROM Goods g WHERE g.id IN ?1 AND g.status = true")
    override fun findAllById(ids: MutableIterable<Long>): MutableList<Goods>
}