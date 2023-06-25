package com.ryouonritsu.ic.config

import com.ryouonritsu.ic.common.annotation.CronJob
import com.ryouonritsu.ic.common.annotation.ScheduledTask
import com.ryouonritsu.ic.repository.CronJobConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.support.CronTrigger

/**
 * @author ryouonritsu
 */
@Configuration
class CronJobConfig(
    private val threadPoolTaskScheduler: ThreadPoolTaskScheduler,
    private val applicationContext: ApplicationContext,
    private val cronJobConfigRepository: CronJobConfigRepository
) : SchedulingConfigurer {
    companion object {
        private val log = LoggerFactory.getLogger(CronJobConfig::class.java)
    }

    /**
     * Callback allowing a [ TaskScheduler][org.springframework.scheduling.TaskScheduler] and specific [Task][org.springframework.scheduling.config.Task]
     * instances to be registered against the given the [ScheduledTaskRegistrar].
     * @param taskRegistrar the registrar to be configured.
     */
    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        taskRegistrar.setScheduler(threadPoolTaskScheduler)

        applicationContext.getBeansWithAnnotation(CronJob::class.java).forEach { (_, v) ->
            AnnotationUtils.findAnnotation(v.javaClass, CronJob::class.java)?.run annotation@{
                log.info("[CronJobConfig.configureTasks] init CronJob: ${this.name}")
                (v as? ScheduledTask)?.run {
                    taskRegistrar.addTriggerTask(this) {
                        cronJobConfigRepository.findAllByNameAndStatusOrderByCreateTimeDesc(this@annotation.name)
                            .firstOrNull()?.run {
                                log.info("[CronJobConfig.configureTasks] set cron task context, name = ${this.name}, expr = ${this.expression}")
                                CronTrigger(this.expression).nextExecutionTime(it)
                            }
                    }
                }
            }
        }
    }
}