package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.Subscriber
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author ryouonritsu
 */
@Repository
interface SubscriberRepository : JpaRepositoryImplementation<Subscriber, Long> {
    @Query("SELECT s FROM Subscriber s WHERE s.tag = ?1 AND s.status = true ORDER BY s.createTime")
    fun findByTag(tag: String): List<Subscriber>

    @Query("SELECT s FROM Subscriber s WHERE s.email = ?1 AND s.status = true")
    fun findByEmail(email: String): List<Subscriber>

    @Query("SELECT s FROM Subscriber s WHERE s.tag = ?1 AND s.email = ?2 AND s.status = true")
    fun findByTagAndEmail(tag: String, email: String): Subscriber?
}