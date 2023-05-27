package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author ryouonritsu
 */
@Repository
interface UserRepository : JpaRepositoryImplementation<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.isDeleted = false")
    fun findByUsername(username: String): User?

    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.isDeleted = false")
    fun findByEmail(email: String): User?

    @Query(
        """
            SELECT *
            FROM user
            WHERE is_deleted = 0
                AND (id = ?1 OR username LIKE CONCAT('%', ?1, '%') OR real_name LIKE CONCAT('%', ?1, '%'))
            LIMIT 10
        """,
        nativeQuery = true
    )
    fun findByKeyword(keyword: String): List<User>

    @Query("SELECT u FROM User u WHERE u.isDeleted = false ORDER BY u.createTime")
    fun list(pageable: Pageable = PageRequest.of(0, 10)): Page<User>
}