package com.ryouonritsu.ic.repository

import com.ryouonritsu.ic.entity.CronJobConfig
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

/**
 * @author ryouonritsu
 */
@Repository
interface CronJobConfigRepository : JpaRepositoryImplementation<CronJobConfig, Long> {
    fun findAllByNameAndStatusOrderByCreateTimeDesc(
        name: String, status: Boolean = true
    ): List<CronJobConfig>
}