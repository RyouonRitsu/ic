package com.ryouonritsu.ic.config

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.ryouonritsu.ic.common.utils.RequestContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

/**
 * @author ryouonritsu
 */
@Configuration
class AsyncConfig(
    @Value("\${thread.pool.maxPoolSize}")
    private val maxPoolSize: Int,
    @Value("\${thread.pool.corePoolSize}")
    private val corePoolSize: Int,
    @Value("\${thread.pool.queueCapacity}")
    private val queueCapacity: Int,
    @Value("\${thread.pool.keepAliveSeconds}")
    private val keepAliveSeconds: Int,
    @Value("\${thread.pool.awaitTerminationMillis}")
    private val awaitTerminationMillis: Int
) {
    @Bean("asyncTaskExecutor")
    fun asyncTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            setThreadFactory(
                ThreadFactoryBuilder()
                    .setNameFormat("ritsu-async-task-thread-pool-%d")
                    .build()
            )
            maxPoolSize = this@AsyncConfig.maxPoolSize
            corePoolSize = this@AsyncConfig.corePoolSize
            queueCapacity = this@AsyncConfig.queueCapacity
            keepAliveSeconds = this@AsyncConfig.keepAliveSeconds
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(this@AsyncConfig.awaitTerminationMillis)
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            setTaskDecorator {
                val user = RequestContext.user
                return@setTaskDecorator Runnable {
                    RequestContext.user = user
                    it.run()
                }
            }
            initialize()
        }
    }
}