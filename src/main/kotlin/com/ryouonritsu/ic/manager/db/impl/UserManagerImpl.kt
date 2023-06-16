package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.manager.db.UserManager
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.impl.UserServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * @author ryouonritsu
 */
@Component
class UserManagerImpl(
    private val redisUtils: RedisUtils,
    private val userRepository: UserRepository,
    private val transactionTemplate: TransactionTemplate,
    @Value("\${mail.service.account}")
    private val mailServiceAccount: String,
    @Value("\${mail.service.password}")
    private val mailServicePassword: String,
    @Value("\${mail.service.nick}")
    private val mailServiceNick: String,
    @Value("\${mail.smtp.auth}")
    private val mailSmtpAuth: String,
    @Value("\${mail.smtp.host}")
    private val mailSmtpHost: String,
    @Value("\${mail.smtp.port}")
    private val mailSmtpPort: String,
) : UserManager {

    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    override fun retrySendEmail(
        email: String,
        subject: String,
        html: String,
        times: Int
    ): Boolean {
        var success = false

        for (i in 1..times) {
            success = sendEmail(email, subject, html)
            if (success) break
        }

        return success
    }

    override fun sendEmail(email: String, subject: String, html: String): Boolean {
        val account = mailServiceAccount
        val password = mailServicePassword
        val nick = mailServiceNick
        val props = mapOf(
            "mail.smtp.auth" to mailSmtpAuth,
            "mail.smtp.host" to mailSmtpHost,
            "mail.smtp.port" to mailSmtpPort
        )
        val properties = Properties().apply { putAll(props) }
        val authenticator = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(account, password)
            }
        }
        val mailSession = Session.getInstance(properties, authenticator)
        val htmlMessage = MimeMessage(mailSession).apply {
            setFrom(InternetAddress(account, nick, "UTF-8"))
            setRecipient(MimeMessage.RecipientType.TO, InternetAddress(email, "", "UTF-8"))
            setSubject(subject, "UTF-8")
            setContent(html, "text/html; charset=UTF-8")
        }
        log.info("Sending email to $email")
        return runCatching { Transport.send(htmlMessage) }
            .onFailure {
                log.error("[UserService.sendEmail] Send email to $email failed", it)
            }.isSuccess
    }

    override fun verifyCodeCheck(
        email: String,
        verifyCode: String?
    ): Pair<Boolean, Response<Unit>?> {
        val vc = redisUtils[email]
        if (vc.isNullOrBlank()) return Pair(
            false, Response.failure("验证码无效")
        )
        if (verifyCode != vc) return Pair(
            false, Response.failure("验证码错误, 请再试一次")
        )
        return Pair(true, null)
    }
}