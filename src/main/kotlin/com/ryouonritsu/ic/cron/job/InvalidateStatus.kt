package com.ryouonritsu.ic.cron.job

import com.alibaba.fastjson2.parseArray
import com.ryouonritsu.ic.common.annotation.CronJob
import com.ryouonritsu.ic.common.annotation.ScheduledTask
import com.ryouonritsu.ic.entity.PaymentInfo
import com.ryouonritsu.ic.entity.RentalInfo
import com.ryouonritsu.ic.entity.User
import com.ryouonritsu.ic.repository.PaymentInfoRepository
import com.ryouonritsu.ic.repository.RentalInfoRepository
import com.ryouonritsu.ic.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

/**
 * @author ryouonritsu
 */
@Component
@CronJob("InvalidateStatus")
class InvalidateStatus(
    private val rentalInfoRepository: RentalInfoRepository,
    private val userRepository: UserRepository,
    private val paymentInfoRepository: PaymentInfoRepository,
    private val transactionTemplate: TransactionTemplate
) : ScheduledTask {
    companion object {
        private val log = LoggerFactory.getLogger(InvalidateStatus::class.java)
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
        log.info("================================ Running InvalidateStatus ================================")
        val allRentalInfos = rentalInfoRepository.findAllByStatus()
        val now = LocalDate.now()
        val invalidRentalInfos = mutableListOf<RentalInfo>()
        val invalidPaymentInfos = mutableListOf<PaymentInfo>()
        val invalidUsers = mutableListOf<User>()
        allRentalInfos.forEach {
            if (!now.isAfter(it.endTime)) return@forEach

            invalidRentalInfos += it.apply { status = false }
            val relatedPaymentInfo = paymentInfoRepository.findAllByRentalIdAndStatus(it.id)
            invalidPaymentInfos += relatedPaymentInfo.onEach { info -> info.status = false }
            val user = userRepository.findByIdAndStatus(it.userId) ?: return@forEach
            if (user.rentalInfoIds.parseArray<Long>()
                    .all { id -> id !in (allRentalInfos - invalidRentalInfos.toSet()).map { info -> info.id } }
            ) invalidUsers += user.apply { status = false }
        }
        transactionTemplate.execute {
            if (invalidRentalInfos.isNotEmpty()) {
                log.info("[InvalidateStatus] invalidating rentalInfo in ${invalidRentalInfos.map { it.id }}")
                rentalInfoRepository.saveAll(invalidRentalInfos)
            }
            if (invalidPaymentInfos.isNotEmpty()) {
                log.info("[InvalidateStatus] invalidating paymentInfo in ${invalidPaymentInfos.map { it.id }}")
                paymentInfoRepository.saveAll(invalidPaymentInfos)
            }
            if (invalidUsers.isNotEmpty()) {
                log.info("[InvalidateStatus] invalidating user in ${invalidUsers.map { it.id }}")
                userRepository.saveAll(invalidUsers)
            }
        }
        log.info("================================ InvalidateStatus Finished ================================")
    }
}