package com.ryouonritsu.ic.cron.job

import com.ryouonritsu.ic.common.annotation.CronJob
import com.ryouonritsu.ic.common.annotation.ScheduledTask
import com.ryouonritsu.ic.common.constants.ICConstant
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.entity.Visitor
import com.ryouonritsu.ic.manager.rpc.SmsService
import com.ryouonritsu.ic.repository.VisitorRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * @author ryouonritsu
 */
@Component
@CronJob("VisitorVerify")
class VisitorVerify(
    private val visitorRepository: VisitorRepository,
    private val redisUtils: RedisUtils,
    private val smsService: SmsService,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val transactionTemplate: TransactionTemplate
) : ScheduledTask {
    companion object {
        private val log = LoggerFactory.getLogger(VisitorVerify::class.java)
    }

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
        log.info("================================ Running VisitorVerify ================================")
        val allVisitors = visitorRepository.findAllByStatus()
        val now = LocalDateTime.now()
        allVisitors.forEach {
            when (Visitor.VisitStatus.valueOf(it.visitStatus)) {
                Visitor.VisitStatus.INACCESSIBLE -> {
                    val checkDateTime = it.visitTime.minusMinutes(ICConstant.LONG_30)
                    log.info("[VisitorVerify] inaccessible checkDateTime is $checkDateTime")
                    if (!checkDateTime.equalsIgnoreSecond(now)) return@forEach

                    threadPoolTaskExecutor.submit {
                        val verificationCode = (1..6).joinToString("") { "${(0..9).random()}" }
                        val success = smsService.sendVerifyCodeSms(it.phoneNumber, verificationCode)
                        if (success) {
                            redisUtils.set(
                                it.phoneNumber, verificationCode, ICConstant.LONG_12, TimeUnit.HOURS
                            )
                            transactionTemplate.execute { _ ->
                                visitorRepository.save(it.apply {
                                    visitStatus = Visitor.VisitStatus.ACCESSIBLE()
                                })
                            }
                        }
                    }
                }

                Visitor.VisitStatus.ACCESSIBLE -> {
                    val checkDateTime = it.visitTime.plusHours(ICConstant.LONG_12)
                        .minusMinutes(ICConstant.LONG_30)
                    log.info("[VisitorVerify] accessible checkDateTime is $checkDateTime")
                    if (!checkDateTime.equalsIgnoreSecond(now)) return@forEach

                    threadPoolTaskExecutor.submit {
                        redisUtils - it.phoneNumber
                        transactionTemplate.execute { _ ->
                            visitorRepository.save(it.apply {
                                visitStatus = Visitor.VisitStatus.EXPIRED()
                            })
                        }
                    }
                }

                else -> return@forEach
            }
        }
        log.info("================================ VisitorVerify Finished ================================")
    }

    private fun LocalDateTime.equalsIgnoreSecond(other: LocalDateTime): Boolean {
        return this.year == other.year && this.month == other.month && this.dayOfMonth == other.dayOfMonth
                && this.hour == other.hour && this.minute == other.minute
    }
}