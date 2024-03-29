package com.ryouonritsu.ic.cron.job

import com.alibaba.fastjson2.parseArray
import com.alibaba.fastjson2.toJSONString
import com.ryouonritsu.ic.common.annotation.CronJob
import com.ryouonritsu.ic.common.annotation.ScheduledTask
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.entity.*
import com.ryouonritsu.ic.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

/**
 * @author ryouonritsu
 */
@Component
@CronJob("InvalidateStatus")
class InvalidateStatus(
    private val rentalInfoRepository: RentalInfoRepository,
    private val userRepository: UserRepository,
    private val paymentInfoRepository: PaymentInfoRepository,
    private val roomRepository: RoomRepository,
    private val invitationCodeRepository: InvitationCodeRepository,
    private val mroRepository: MRORepository,
    private val eventRepository: EventRepository,
    private val visitorRepository: VisitorRepository,
    private val transactionTemplate: TransactionTemplate,
    private val redisUtils: RedisUtils
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
        val allUsers = userRepository.findAllByStatus().associateBy { it.id }
        val now = LocalDate.now()
        val invalidRentalInfos = mutableListOf<RentalInfo>()
        val invalidPaymentInfos = mutableListOf<PaymentInfo>()
        val invalidMROs = mutableListOf<MRO>()
        val invalidEvents = mutableListOf<Event>()
        val invalidVisitors = mutableListOf<Visitor>()
        val invalidInvitationCode = mutableListOf<InvitationCode>()
        val changedUsers = mutableMapOf<Long, User>()
        val availableRooms = mutableListOf<Room>()

        allUsers.forEach { (id, user) ->
            if (user.rentalInfoIds.parseArray().isEmpty()
                && user.userType == User.UserType.CLIENT()
            ) changedUsers[id] = user.apply { status = false }
        }

        allRentalInfos.forEach {
            if (!now.isAfter(it.endTime)) return@forEach

            // invalidate rentalInfo
            invalidRentalInfos += it.apply { status = false }

            // invalidate paymentInfos
            val relatedPaymentInfo = paymentInfoRepository.findAllByRentalIdAndStatus(it.id)
            invalidPaymentInfos += relatedPaymentInfo.onEach { info -> info.status = false }

            // free room
            roomRepository.findById(it.roomId).getOrNull()?.run {
                availableRooms += this.apply {
                    userId = null
                    commence = null
                    terminate = null
                    contract = null
                    status = false
                }
            }

            // change user
            val user = changedUsers[it.userId]
                ?: allUsers[it.userId]
                ?: return@forEach
            user.paymentInfoIds = run {
                val ids = user.paymentInfoIds.parseArray<Long>()
                ids -= relatedPaymentInfo.map { info -> info.id }.toSet()
                ids.toJSONString()
            }
            val (ids, rentalInfoIds) = run {
                val ids = user.rentalInfoIds.parseArray<Long>()
                ids -= it.id
                Pair(ids, ids.toJSONString())
            }
            user.rentalInfoIds = rentalInfoIds
            changedUsers[user.id] = user.apply {
                if (ids.isEmpty() && user.userType == User.UserType.CLIENT()) status = false
            }
        }

        changedUsers.forEach { (id, user) ->
            if (!user.status) {
                // invalidate invitationCode
                val invitationCode = invitationCodeRepository.findAllByUserIdAndStatus(id)
                invalidInvitationCode += invitationCode.onEach { c -> c.status = false }

                // invalidate mro
                val mro = mroRepository.findAllByCustomIdAndStatus(id)
                invalidMROs += mro.onEach { m -> m.status = false }

                // invalidate events
                val events = eventRepository.findAllByUserIdAndStatus(id)
                invalidEvents += events.onEach { e -> e.status = false }

                // invalidate visitors
                val visitors = visitorRepository.findAllByCustomIdAndStatus(id)
                invalidVisitors += visitors.onEach { v -> v.status = false }
            }
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
            if (invalidInvitationCode.isNotEmpty()) {
                log.info("[InvalidateStatus] invalidating invitationCode in ${invalidInvitationCode.map { it.id }}")
                invitationCodeRepository.saveAll(invalidInvitationCode)
            }
            if (invalidMROs.isNotEmpty()) {
                log.info("[InvalidateStatus] invalidating mro in ${invalidMROs.map { it.id }}")
                mroRepository.saveAll(invalidMROs)
            }
            if (invalidEvents.isNotEmpty()) {
                log.info("[InvalidateStatus] invalidating events in ${invalidEvents.map { it.id }}")
                eventRepository.saveAll(invalidEvents)
            }
            if (invalidVisitors.isNotEmpty()) {
                log.info("[InvalidateStatus] invalidating visitors in ${invalidVisitors.map { it.id }}")
                visitorRepository.saveAll(invalidVisitors)
                invalidVisitors.map { it.phoneNumber }.forEach { redisUtils - it }
            }
            if (availableRooms.isNotEmpty()) {
                log.info("[InvalidateStatus] freeing room in ${availableRooms.map { it.id }}")
                roomRepository.saveAll(availableRooms)
            }
            if (changedUsers.isNotEmpty()) {
                log.info("[InvalidateStatus] changing user in ${changedUsers.keys}")
                userRepository.saveAll(changedUsers.values)
            }
        }
        log.info("================================ InvalidateStatus Finished ================================")
    }
}