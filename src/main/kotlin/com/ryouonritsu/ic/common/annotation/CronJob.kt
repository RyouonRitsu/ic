package com.ryouonritsu.ic.common.annotation

import org.springframework.stereotype.Component

/**
 * @author ryouonritsu
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CronJob(
    val name: String
)

/**
 * @author ryouonritsu
 */
interface ScheduledTask : Runnable