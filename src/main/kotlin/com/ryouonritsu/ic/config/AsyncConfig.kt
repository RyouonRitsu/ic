package com.ryouonritsu.ic.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

/**
 * @author ryouonritsu
 */
@Configuration
class AsyncConfig {
    companion object {
        const val MAX_POOL_SIZE = 20
        const val CORE_POOL_SIZE = 5
        const val QUEUE_CAPACITY = 200
        const val AWAIT_TERMINATION_SECONDS = 60
    }

    @Bean("asyncTaskExecutor")
    fun asyncTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            maxPoolSize = MAX_POOL_SIZE
            corePoolSize = CORE_POOL_SIZE
            queueCapacity = QUEUE_CAPACITY
            setThreadNamePrefix("ritsu-async-task-thread-pool-")
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS)
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }
    }
}