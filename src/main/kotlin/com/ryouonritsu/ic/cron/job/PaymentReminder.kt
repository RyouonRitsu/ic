package com.ryouonritsu.ic.cron.job

import com.alibaba.fastjson2.toJSONString
import com.ryouonritsu.ic.common.annotation.CronJob
import com.ryouonritsu.ic.common.annotation.ScheduledTask
import com.ryouonritsu.ic.common.constants.ICConstant
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.entity.Event
import com.ryouonritsu.ic.manager.db.NotificationManager
import com.ryouonritsu.ic.repository.PaymentInfoRepository
import com.ryouonritsu.ic.repository.RentalInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * @author ryouonritsu
 */
@Component
@CronJob("PaymentReminder")
class PaymentReminder(
    private val rentalInfoRepository: RentalInfoRepository,
    private val paymentInfoRepository: PaymentInfoRepository,
    private val notificationManager: NotificationManager,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor
) : ScheduledTask {
    companion object {
        private val log = LoggerFactory.getLogger(PaymentReminder::class.java)
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
        log.info("================================ Running PaymentReminder ================================")
        val allRentalInfos = rentalInfoRepository.findAllByStatus()
        val now = LocalDate.now()
        allRentalInfos.forEach { rentalInfo ->
            var expireDateOfThisYear = rentalInfo.endTime
            while (expireDateOfThisYear.year > now.year)
                expireDateOfThisYear = expireDateOfThisYear.minusYears(ICConstant.LONG_1)
            val checkDate = expireDateOfThisYear.minusMonths(ICConstant.LONG_1)
            log.info("[PaymentReminder] expireDateOfThisYear is $expireDateOfThisYear, checkData is $checkDate")
            if (now != checkDate) return@forEach

            val yearsOfPaymentInfos =
                paymentInfoRepository.findAllByRentalIdAndStatus(rentalInfo.id)
                    .map { it.createTime.year }
                    .toSet()
            if (now.year in yearsOfPaymentInfos) return@forEach

            val f = threadPoolTaskExecutor.submitListenable<Event> {
                return@submitListenable notificationManager.publish(
                    PublishRequest(
                        userId = rentalInfo.userId,
                        name = ICConstant.PAYMENT_REMINDER_NAME,
                        message = String.format(
                            ICConstant.PAYMENT_REMINDER_MESSAGE,
                            expireDateOfThisYear.format(DateTimeFormatter.ISO_DATE)
                        )
                    )
                )
            }
            f.addCallback(
                {
                    it?.run {
                        log.info("[PaymentReminder] reminder to [${rentalInfo.userId}] successful, event = ${this.toJSONString()}")
                    }
                        ?: log.error("[PaymentReminder] unexpected reminder to [${rentalInfo.userId}], missing event!")
                },
                { log.error("[PaymentReminder] reminder to [${rentalInfo.userId}] failed!", it) }
            )
        }
        log.info("================================ PaymentReminder Finished ================================")
    }
}