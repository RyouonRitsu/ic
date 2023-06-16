package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.InvitationCode
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author ryouonritsu
 */
@Repository
interface InvitationCodeRepository : JpaRepositoryImplementation<InvitationCode, Long> {
    fun findByIdAndStatus(id: Long, status: Boolean = true): InvitationCode?
    fun findAllByCodeAndStatus(code: String, status: Boolean = true): List<InvitationCode>
    fun findAllByUserIdAndStatus(userId: Long, status: Boolean = true): List<InvitationCode>
}