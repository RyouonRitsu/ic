package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.MD5Util
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.common.utils.TokenUtils
import com.ryouonritsu.ic.domain.dto.UserDTO
import com.ryouonritsu.ic.domain.protocol.request.ModifyUserInfoRequest
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.User
import com.ryouonritsu.ic.entity.UserFile
import com.ryouonritsu.ic.repository.*
import com.ryouonritsu.ic.service.UserService
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.io.path.Path

/**
 * @author ryouonritsu
 */
@Service
class UserServiceImpl(
    private val redisUtils: RedisUtils,
    private val userRepository: UserRepository,
    private val userFileRepository: UserFileRepository,
    @Value("\${static.file.prefix}")
    private val staticFilePrefix: String
) : UserService {
    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    private fun getHtml(url: String): Pair<Int, String?> {
        val client = OkHttpClient()
        val request = Request.Builder().get().url(url).build()
        return try {
            val response = client.newCall(request).execute()
            when (response.code) {
                200 -> Pair(200, response.body?.string())
                else -> Pair(response.code, null)
            }
        } catch (e: Exception) {
            Pair(500, e.message)
        }
    }

    private fun sendEmail(email: String, subject: String, html: String): Boolean {
        val account = "inkbook_ritsu@163.com"
        val password = "GMIRTTQDLBMWOROX"
        val nick = "BUAA Official"
        val props = mapOf(
            "mail.smtp.auth" to "true",
            "mail.smtp.host" to "smtp.163.com",
            "mail.smtp.port" to "25"
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

    private fun check(
        email: String,
        username: String,
        password: String,
        realName: String
    ): Pair<Boolean, Response<Unit>?> {
        if (!email.matches(Regex("[\\w\\\\.]+@[\\w\\\\.]+\\.\\w+"))) return Pair(
            false, Response.failure("邮箱格式不正确")
        )
        if (username.length > 50) return Pair(
            false, Response.failure("用户名长度不能超过50")
        )
        if (password.length < 8 || password.length > 30) return Pair(
            false, Response.failure("密码长度必须在8-30之间")
        )
        if (realName.length > 50) return Pair(
            false, Response.failure("真实姓名长度不能超过50")
        )
        return Pair(true, null)
    }

    private fun emailCheck(email: String?): Pair<Boolean, Response<Unit>?> {
        if (email.isNullOrBlank()) return Pair(
            false, Response.failure("邮箱不能为空")
        )
        if (!email.matches(Regex("[\\w\\\\.]+@[\\w\\\\.]+\\.\\w+"))) return Pair(
            false, Response.failure("邮箱格式不正确")
        )
        return Pair(true, null)
    }

    private fun sendVerifyCodeEmailUseTemplate(
        template: String,
        verificationCode: String,
        email: String,
        subject: String
    ): Response<Unit> {
        // 此处需替换成服务器地址!!!
//        val (code, html) = getHtml("http://101.42.171.88:8090/registration_verification?verification_code=$verification_code")
        val (code, html) = getHtml("http://localhost:8090/$template?verification_code=$verificationCode")
        val success = if (code == 200 && html != null) sendEmail(email, subject, html) else false
        return if (success) {
            redisUtils.set("verification_code", verificationCode, 5, TimeUnit.MINUTES)
            redisUtils.set("email", email, 5, TimeUnit.MINUTES)
            Response.success("验证码已发送")
        } else Response.failure("验证码发送失败")
    }

    override fun sendRegistrationVerificationCode(
        email: String?,
        modify: Boolean
    ): Response<Unit> {
        val (result, message) = emailCheck(email)
        if (!result && message != null) return message
        val t = userRepository.findByEmail(email!!)
        if (t != null) return Response.failure("该邮箱已被注册")
        val subject = if (modify) "BUAA校友信息收集修改邮箱验证码" else "BUAA校友信息收集邮箱注册验证码"
        val verificationCode = (1..6).joinToString("") { "${(0..9).random()}" }
        return sendVerifyCodeEmailUseTemplate(
            "registration_verification",
            verificationCode,
            email,
            subject
        )
    }

    private fun verifyCodeCheck(verifyCode: String?): Pair<Boolean, Response<Unit>?> {
        val vc = redisUtils["verification_code"]
        if (vc.isNullOrBlank()) return Pair(
            false, Response.failure("验证码无效")
        )
        if (verifyCode != vc) return Pair(
            false, Response.failure("验证码错误, 请再试一次")
        )
        redisUtils - "verification_code"
        return Pair(true, null)
    }

    override fun register(
        email: String?,
        verificationCode: String?,
        username: String?,
        password1: String?,
        password2: String?,
        avatar: String,
        realName: String
    ): Response<Unit> {
        if (email.isNullOrBlank()) return Response.failure("邮箱不能为空")
        if (verificationCode.isNullOrBlank()) return Response.failure("验证码不能为空")
        if (username.isNullOrBlank()) return Response.failure("用户名不能为空")
        if (password1.isNullOrBlank()) return Response.failure("密码不能为空")
        if (password2.isNullOrBlank()) return Response.failure("确认密码不能为空")
        val (result, message) = check(email, username, password1, realName)
        if (!result && message != null) return message
        val t = userRepository.findByEmail(email)
        if (t != null) return Response.failure("该邮箱已被注册")
        return runCatching {
            val (re, msg) = verifyCodeCheck(verificationCode)
            if (!re && msg != null) return@runCatching msg
            if (redisUtils["email"] != email) return Response.failure("该邮箱与验证邮箱不匹配")
            val temp = userRepository.findByUsername(username)
            if (temp != null) return Response.failure("用户名已存在")
            if (password1 != password2) return Response.failure("两次输入的密码不一致")
            userRepository.save(
                User(
                    email = email,
                    username = username,
                    password = MD5Util.encode(password1),
                    realName = realName,
                    avatar = avatar
                )
            )
            Response.success("注册成功")
        }.onFailure { log.error(it.stackTraceToString()) }
            .getOrDefault(Response.failure("注册失败, 发生意外错误"))
    }

    override fun login(
        username: String?,
        password: String?,
        keepLogin: Boolean
    ): Response<List<Map<String, String>>> {
        if (username.isNullOrBlank()) return Response.failure("用户名不能为空")
        if (password.isNullOrBlank()) return Response.failure("密码不能为空")
        return runCatching {
            val user =
                userRepository.findByUsername(username) ?: return Response.failure("用户不存在")
            if (MD5Util.encode(password) != user.password) return Response.failure("密码错误")
            val token = TokenUtils.sign(user)
            if (keepLogin) redisUtils["${user.id}"] = token
            else redisUtils.set("${user.id}", token, 3, TimeUnit.DAYS)
            Response.success(
                "登录成功", listOf(
                    mapOf(
                        "token" to token,
                        "user_id" to "${user.id}"
                    )
                )
            )
        }.onFailure { log.error(it.stackTraceToString()) }
            .getOrDefault(Response.failure("登录失败, 发生意外错误"))
    }

    override fun showInfo(userId: Long): Response<List<UserDTO>> {
        return runCatching {
            val user = userRepository.findById(userId).get()
            Response.success("获取成功", listOf(user.toDTO()))
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "$userId"
                return Response.failure("数据库中没有此用户, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(
            Response.failure("获取失败, 发生意外错误")
        )
    }

    override fun selectUserByUserId(userId: Long): Response<List<UserDTO>> {
        return runCatching {
            val user = userRepository.findById(userId).get()
            Response.success("获取成功", listOf(user.toDTO()))
        }.onFailure {
            if (it is NoSuchElementException) return Response.failure("数据库中没有此用户")
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("获取失败, 发生意外错误"))
    }

    override fun sendForgotPasswordEmail(email: String?): Response<Unit> {
        val (result, message) = emailCheck(email)
        if (!result && message != null) return message
        userRepository.findByEmail(email!!) ?: return Response.failure("该邮箱未被注册")
        val subject = "BUAA校友信息收集邮箱找回密码验证码"
        val verificationCode = (1..6).joinToString("") { "${(0..9).random()}" }
        return sendVerifyCodeEmailUseTemplate(
            "forgot_password",
            verificationCode,
            email,
            subject
        )
    }

    override fun changePassword(
        mode: Int?,
        oldPassword: String?,
        password1: String?,
        password2: String?,
        email: String?,
        verifyCode: String?
    ): Response<Unit> {
        when (mode) {
            0 -> {
                val (result, message) = verifyCodeCheck(verifyCode)
                if (!result && message != null) return message
                if (password1.isNullOrBlank() || password2.isNullOrBlank()) return Response.failure(
                    "密码不能为空"
                )
                if (password1 != password2) return Response.failure("两次密码不一致")
                val (re, msg) = emailCheck(email)
                if (!re && msg != null) return msg
                return runCatching {
                    val user = userRepository.findByEmail(email!!)
                        ?: return Response.failure("该邮箱未被注册, 发生意外错误, 请检查数据库")
                    user.password = MD5Util.encode(password1)
                    userRepository.save(user)
                    redisUtils - "${user.id}"
                    Response.success<Unit>("修改成功")
                }.onFailure { log.error(it.stackTraceToString()) }
                    .getOrDefault(Response.failure("修改失败, 发生意外错误"))
            }

            1 -> {
                return runCatching {
                    val user = userRepository.findById(RequestContext.userId.get()!!).get()
                    if (password1.isNullOrBlank() || password2.isNullOrBlank() || oldPassword.isNullOrBlank()) return Response.failure(
                        "密码不能为空"
                    )
                    if (MD5Util.encode(user.password) != oldPassword) return Response.failure("原密码错误")
                    if (password1.length < 8 || password1.length > 30) return Response.failure("密码长度必须在8-30位之间")
                    if (password1 != password2) return Response.failure("两次密码不一致")
                    user.password = MD5Util.encode(password1)
                    userRepository.save(user)
                    redisUtils - "${user.id}"
                    Response.success<Unit>("修改成功")
                }.onFailure {
                    if (it is NoSuchElementException) {
                        redisUtils - "${RequestContext.userId.get()}"
                        return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
                    }
                    log.error(it.stackTraceToString())
                }.getOrDefault(
                    Response.failure("修改失败, 发生意外错误")
                )
            }

            else -> return Response.failure("修改模式不在合法范围内, 应为0或1")
        }
    }

    override fun uploadFile(
        file: MultipartFile
    ): Response<List<Map<String, String>>> {
        return runCatching {
            if (file.size >= 10 * 1024 * 1024) return Response.failure("上传失败, 文件大小超过最大限制10MB！")
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS_")
            val time = LocalDateTime.now().format(formatter)
            val userId = RequestContext.userId.get()
            val fileDir = "static/file/${userId}"
            val fileName = time + file.originalFilename
            val filePath = "$fileDir/$fileName"
            if (!File(fileDir).exists()) File(fileDir).mkdirs()
            file.transferTo(Path(filePath))
            val fileUrl = "http://$staticFilePrefix:8090/file/${userId}/${fileName}"
            userFileRepository.save(
                UserFile(
                    url = fileUrl,
                    filePath = filePath,
                    fileName = fileName,
                    userId = userId!!
                )
            )
            Response.success(
                "上传成功", listOf(
                    mapOf(
                        "url" to fileUrl
                    )
                )
            )
        }.onFailure { log.error(it.stackTraceToString()) }
            .getOrDefault(Response.failure("上传失败, 发生意外错误"))
    }

    override fun deleteFile(url: String): Response<Unit> {
        return try {
            val file = userFileRepository.findByUrl(url) ?: return Response.failure(
                "文件不存在"
            )
            File(file.filePath).delete()
            userFileRepository.delete(file)
            Response.failure("删除成功")
        } catch (e: Exception) {
            log.error(e.stackTraceToString())
            Response.failure("删除失败, 发生意外错误")
        }
    }

    override fun modifyUserInfo(request: ModifyUserInfoRequest): Response<Unit> {
        return runCatching {
            val user = userRepository.findById(RequestContext.userId.get()!!).get()
            if (!request.username.isNullOrBlank()) {
                val t = userRepository.findByUsername(request.username)
                if (t != null) return Response.failure("用户名已存在")
                if (request.username.length > 50) return Response.failure("用户名长度不能超过50")
                user.username = request.username
            }
            if (!request.realName.isNullOrBlank()) {
                if (request.realName.length > 50) return Response.failure("真实姓名长度不能超过50")
                user.realName = request.realName
            }
            if (!request.avatar.isNullOrBlank()) {
                user.avatar = request.avatar
            }
            if (!request.educationalBackground.isNullOrBlank()) {
                user.educationalBackground = request.educationalBackground
            }
            if (request.isAdmin != null) {
                user.isAdmin = request.isAdmin
            }
            userRepository.save(user)
            Response.success<Unit>("修改成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.userId.get()}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("修改失败, 发生意外错误"))
    }

    override fun modifyEmail(
        email: String?,
        verifyCode: String?,
        password: String?
    ): Response<Unit> {
        return runCatching {
            val user = userRepository.findById(RequestContext.userId.get()!!).get()
            val (result, message) = emailCheck(email)
            if (!result && message != null) return message
            val t = userRepository.findByEmail(email!!)
            if (t != null) return Response.failure("该邮箱已被注册")
            val (re, msg) = verifyCodeCheck(verifyCode)
            if (!re && msg != null) return@runCatching msg
            if (redisUtils["email"] != email) return Response.failure("该邮箱与验证邮箱不匹配")
            if (MD5Util.encode(password) != user.password) return Response.failure("密码错误")
            val (code, html) = getHtml("http://localhost:8090/change_email?email=${email}")
            val success =
                if (code == 200 && html != null) sendEmail(
                    user.email,
                    "BUAA校友信息收集邮箱修改通知",
                    html
                ) else false
            if (!success) throw Exception("邮件发送失败")
            user.email = email
            userRepository.save(user)
            Response.success("修改成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.userId.get()}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            if (it.message != null) return Response.failure("${it.message}")
            else log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("修改失败, 发生意外错误"))
    }
}