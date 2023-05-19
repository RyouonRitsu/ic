package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.constants.ICConstant
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.domain.dto.EventDTO
import com.ryouonritsu.ic.domain.dto.SubscriberDTO
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.domain.protocol.request.SubscribeRequest
import com.ryouonritsu.ic.domain.protocol.request.UnsubscribeRequest
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.Event
import com.ryouonritsu.ic.entity.Subscriber
import com.ryouonritsu.ic.repository.EventRepository
import com.ryouonritsu.ic.repository.SubscriberRepository
import com.ryouonritsu.ic.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * @author ryouonritsu
 */
@Service
class NotificationServiceImpl(
    private val redisUtils: RedisUtils,
    private val userServiceImpl: UserServiceImpl,
    private val eventRepository: EventRepository,
    private val subscriberRepository: SubscriberRepository
) : NotificationService {
    companion object {
        private val log = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    }

    override fun publish(request: PublishRequest): Response<EventDTO> {
        val event = Event(
            tag = request.tag!!,
            name = request.name ?: ICConstant.EVENT,
            message = request.message!!
        )

        val subscribers = subscriberRepository.findByTag(request.tag)
        val statuses = mutableListOf<Boolean>()
        subscribers.forEach {
            log.info("[publish] sending email to ${it.email}")
            statuses += userServiceImpl.sendEmail(
                it.email,
                "[${event.tag}] ${event.name}",
                event.message
            )
        }

        log.info("[publish] send result: $statuses")
        val published = statuses.any { it }
        event.status = !published
        eventRepository.save(event)
        return Response.success(event.toDTO())
    }

    override fun subscribe(request: SubscribeRequest): Response<SubscriberDTO> {
        run {
            val subscriber = subscriberRepository.findByTagAndEmail(request.tag!!, request.email!!)
            if (subscriber != null) return Response.failure("该邮箱已订阅该事件通知")
        }

        val subscriber = Subscriber(
            tag = request.tag!!,
            email = request.email!!
        )
        subscriberRepository.save(subscriber)
        userServiceImpl.sendEmail(
            subscriber.email,
            "Subscribe Success",
            "您已成功订阅 [${subscriber.tag}] 事件通知"
        )

        val events = eventRepository.findByTagAndStatus(subscriber.tag, true)
        events.forEach {
            val published = userServiceImpl.sendEmail(
                subscriber.email,
                "[${it.tag}] ${it.name}",
                it.message
            )
            log.info("[subscribe] send result: $published")
            if (it.status != !published) {
                it.status = !published
                eventRepository.save(it)
            }
        }

        return Response.success(subscriber.toDTO())
    }

    override fun unsubscribe(request: UnsubscribeRequest): Response<Unit> {
        val subscriber = subscriberRepository.findByTagAndEmail(request.tag!!, request.email!!)
            ?: return Response.failure("该邮箱尚未订阅该事件通知")
        val (success, response) = userServiceImpl.verifyCodeCheck(request.verificationCode)
        if (!success) return response!!
        subscriber.status = false
        subscriberRepository.save(subscriber)
        userServiceImpl.sendEmail(
            subscriber.email,
            "Unsubscribe Success",
            "您已成功退订 [${subscriber.tag}] 事件通知"
        )
        return Response.success("退订成功")
    }

    override fun sendVerificationCode(email: String): Response<Unit> {
        subscriberRepository.findByEmail(email).let {
            if (it.isEmpty()) return Response.failure("该邮箱尚未订阅任何事件通知")
        }
        val verificationCode = (1..6).joinToString("") { "${(0..9).random()}" }
        val success = userServiceImpl.sendEmail(email, "Verification Code", verificationCode)
        return if (success) {
            redisUtils.set("verification_code", verificationCode, 5, TimeUnit.MINUTES)
            redisUtils.set("email", email, 5, TimeUnit.MINUTES)
            Response.success("验证码已发送")
        } else Response.failure("验证码发送失败")
    }
}