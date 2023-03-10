package com.ryouonritsu.ic.service.impl

import com.alibaba.fastjson2.parseArray
import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.toJSONString
import com.ryouonritsu.ic.common.constants.ICConstant
import com.ryouonritsu.ic.common.constants.TemplateType
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.*
import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.component.file.ExcelSheetDefinition
import com.ryouonritsu.ic.component.file.converter.UserUploadConverter
import com.ryouonritsu.ic.component.getTemplate
import com.ryouonritsu.ic.component.process
import com.ryouonritsu.ic.component.read
import com.ryouonritsu.ic.domain.dto.SchoolInfoDTO
import com.ryouonritsu.ic.domain.dto.SocialInfoDTO
import com.ryouonritsu.ic.domain.dto.UserDTO
import com.ryouonritsu.ic.domain.dto.UserInfoDTO
import com.ryouonritsu.ic.domain.protocol.request.ModifyUserInfoRequest
import com.ryouonritsu.ic.domain.protocol.response.ListUserResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.User
import com.ryouonritsu.ic.entity.UserFile
import com.ryouonritsu.ic.repository.*
import com.ryouonritsu.ic.service.UserService
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDate
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
import javax.persistence.criteria.Predicate
import kotlin.io.path.Path

/**
 * @author ryouonritsu
 */
@Service
class UserServiceImpl(
    private val redisUtils: RedisUtils,
    private val userRepository: UserRepository,
    private val userFileRepository: UserFileRepository,
    private val tableTemplateRepository: TableTemplateRepository,
    @Value("\${static.file.prefix}")
    private val staticFilePrefix: String,
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
    @Value("\${mail.text.change-email}")
    private val mailTextChangeEmail: String,
    @Value("\${mail.text.register}")
    private val mailTextRegister: String,
    @Value("\${mail.text.forget-password}")
    private val mailTextForgetPassword: String,
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

    private fun check(
        email: String,
        username: String,
        password: String,
        realName: String
    ): Pair<Boolean, Response<Unit>?> {
        if (!email.matches(Regex("[\\w\\\\.]+@[\\w\\\\.]+\\.\\w+"))) return Pair(
            false, Response.failure("?????????????????????")
        )
        if (username.length > 255) return Pair(
            false, Response.failure("???????????????????????????255")
        )
        if (password.length > 255) return Pair(
            false, Response.failure("????????????????????????255")
        )
        if (realName.length > 255) return Pair(
            false, Response.failure("??????????????????????????????255")
        )
        return Pair(true, null)
    }

    private fun emailCheck(email: String?): Pair<Boolean, Response<Unit>?> {
        if (email.isNullOrBlank()) return Pair(
            false, Response.failure("??????????????????")
        )
        if (!email.matches(Regex("[\\w\\\\.]+@[\\w\\\\.]+\\.\\w+"))) return Pair(
            false, Response.failure("?????????????????????")
        )
        return Pair(true, null)
    }

    private fun sendVerifyCodeEmailUseTemplate(
        template: String,
        verificationCode: String,
        email: String,
        subject: String
    ): Response<Unit> {
        // ?????????????????????????????????!!!
//        val (code, html) = getHtml("http://101.42.171.88:8090/registration_verification?verification_code=$verification_code")
        val (code, html) = getHtml("http://localhost:8090/$template?verification_code=$verificationCode")
        val success = if (code == 200 && html != null) sendEmail(email, subject, html) else false
        return if (success) {
            redisUtils.set("verification_code", verificationCode, 5, TimeUnit.MINUTES)
            redisUtils.set("email", email, 5, TimeUnit.MINUTES)
            Response.success("??????????????????")
        } else Response.failure("?????????????????????")
    }

    override fun sendRegistrationVerificationCode(
        email: String?,
        modify: Boolean
    ): Response<Unit> {
        val (result, message) = emailCheck(email)
        if (!result && message != null) return message
        val t = userRepository.findByEmail(email!!)
        if (t != null) return Response.failure("?????????????????????")
        val subject = if (modify) mailTextChangeEmail else mailTextRegister
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
            false, Response.failure("???????????????")
        )
        if (verifyCode != vc) return Pair(
            false, Response.failure("???????????????, ???????????????")
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
        if (email.isNullOrBlank()) return Response.failure("??????????????????")
        if (verificationCode.isNullOrBlank()) return Response.failure("?????????????????????")
        if (username.isNullOrBlank()) return Response.failure("?????????????????????")
        if (password1.isNullOrBlank()) return Response.failure("??????????????????")
        if (password2.isNullOrBlank()) return Response.failure("????????????????????????")
        val (result, message) = check(email, username, password1, realName)
        if (!result && message != null) return message
        val t = userRepository.findByEmail(email)
        if (t != null) return Response.failure("?????????????????????")
        return runCatching {
            val (re, msg) = verifyCodeCheck(verificationCode)
            if (!re && msg != null) return@runCatching msg
            if (redisUtils["email"] != email) return Response.failure("?????????????????????????????????")
            val temp = userRepository.findByUsername(username)
            if (temp != null) return Response.failure("??????????????????")
            if (password1 != password2) return Response.failure("??????????????????????????????")
            userRepository.save(
                User(
                    email = email,
                    username = username,
                    password = MD5Util.encode(password1),
                    realName = realName,
                    avatar = avatar
                )
            )
            Response.success("????????????")
        }.onFailure { log.error(it.stackTraceToString()) }
            .getOrDefault(Response.failure("????????????, ??????????????????"))
    }

    override fun login(
        username: String?,
        password: String?,
        keepLogin: Boolean
    ): Response<List<Map<String, String>>> {
        if (username.isNullOrBlank()) return Response.failure("?????????????????????")
        if (password.isNullOrBlank()) return Response.failure("??????????????????")
        return runCatching {
            val user =
                userRepository.findByUsername(username) ?: return Response.failure("???????????????")
            if (MD5Util.encode(password) != user.password) return Response.failure("????????????")
            val token = TokenUtils.sign(user)
            if (keepLogin) redisUtils["${user.id}"] = token
            else redisUtils.set("${user.id}", token, 3, TimeUnit.DAYS)
            Response.success(
                "????????????", listOf(
                    mapOf(
                        "token" to token,
                        "user_id" to "${user.id}"
                    )
                )
            )
        }.onFailure { log.error(it.stackTraceToString()) }
            .getOrDefault(Response.failure("????????????, ??????????????????"))
    }

    override fun showInfo(userId: Long): Response<List<UserDTO>> {
        return runCatching {
            val user = userRepository.findById(userId).get()
            Response.success("????????????", listOf(user.toDTO()))
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "$userId"
                return Response.failure("???????????????????????????, ??????????????????")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(
            Response.failure("????????????, ??????????????????")
        )
    }

    override fun selectUserByUserId(userId: Long): Response<List<UserDTO>> {
        return runCatching {
            val user = userRepository.findById(userId).get()
            Response.success("????????????", listOf(user.toDTO()))
        }.onFailure {
            if (it is NoSuchElementException) return Response.failure("???????????????????????????")
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("????????????, ??????????????????"))
    }

    override fun sendForgotPasswordEmail(email: String?): Response<Unit> {
        val (result, message) = emailCheck(email)
        if (!result && message != null) return message
        userRepository.findByEmail(email!!) ?: return Response.failure("?????????????????????")
        val subject = mailTextForgetPassword
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
                    "??????????????????"
                )
                if (password1 != password2) return Response.failure("?????????????????????")
                val (re, msg) = emailCheck(email)
                if (!re && msg != null) return msg
                return runCatching {
                    val user = userRepository.findByEmail(email!!)
                        ?: return Response.failure("?????????????????????, ??????????????????, ??????????????????")
                    user.password = MD5Util.encode(password1)
                    userRepository.save(user)
                    redisUtils - "${user.id}"
                    Response.success<Unit>("????????????")
                }.onFailure { log.error(it.stackTraceToString()) }
                    .getOrDefault(Response.failure("????????????, ??????????????????"))
            }

            1 -> {
                return runCatching {
                    val user = userRepository.findById(
                        RequestContext.userId.get()
                            ?: return Response.failure("????????????????????????, ?????????!")
                    ).get()
                    if (password1.isNullOrBlank() || password2.isNullOrBlank() || oldPassword.isNullOrBlank()) return Response.failure(
                        "??????????????????"
                    )
                    if (MD5Util.encode(oldPassword) != user.password) return Response.failure("???????????????")
                    if (password1.length < 8 || password1.length > 30) return Response.failure("?????????????????????8-30?????????")
                    if (password1 != password2) return Response.failure("?????????????????????")
                    user.password = MD5Util.encode(password1)
                    userRepository.save(user)
                    redisUtils - "${user.id}"
                    Response.success<Unit>("????????????")
                }.onFailure {
                    if (it is NoSuchElementException) {
                        redisUtils - "${RequestContext.userId.get()}"
                        return Response.failure("???????????????????????????????????????token????????????, ??????????????????")
                    }
                    log.error(it.stackTraceToString())
                }.getOrDefault(
                    Response.failure("????????????, ??????????????????")
                )
            }

            else -> return Response.failure("?????????????????????????????????, ??????0???1")
        }
    }

    override fun uploadFile(
        file: MultipartFile
    ): Response<List<Map<String, String>>> {
        return runCatching {
            if (file.size >= 10 * 1024 * 1024) return Response.failure("????????????, ??????????????????????????????10MB???")
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
                "????????????", listOf(
                    mapOf(
                        "url" to fileUrl
                    )
                )
            )
        }.onFailure { log.error(it.stackTraceToString()) }
            .getOrDefault(Response.failure("????????????, ??????????????????"))
    }

    override fun deleteFile(url: String): Response<Unit> {
        return try {
            val file = userFileRepository.findByUrl(url) ?: return Response.failure(
                "???????????????"
            )
            File(file.filePath).delete()
            userFileRepository.delete(file)
            Response.failure("????????????")
        } catch (e: Exception) {
            log.error(e.stackTraceToString())
            Response.failure("????????????, ??????????????????")
        }
    }

    override fun modifyUserInfo(request: ModifyUserInfoRequest): Response<Unit> {
        return runCatching {
            val user = userRepository.findById(RequestContext.userId.get()!!).get()
            if (!request.username.isNullOrBlank()) {
                val t = userRepository.findByUsername(request.username)
                if (t != null) return Response.failure("??????????????????")
                if (request.username.length > 50) return Response.failure("???????????????????????????50")
                user.username = request.username
            }
            if (!request.avatar.isNullOrBlank()) {
                user.avatar = request.avatar
            }
            if (!request.realName.isNullOrBlank()) {
                if (request.realName.length > 50) return Response.failure("??????????????????????????????50")
                user.realName = request.realName
            }
            if (!request.gender.isNullOrBlank()) {
                user.gender = User.Gender.getByDesc(request.gender).code
            }
            if (!request.birthday.isNullOrBlank()) {
                try {
                    user.birthday =
                        LocalDate.parse(request.birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    return Response.failure("??????????????????, ??????yyyy-MM-dd")
                }
            }
            if (!request.phone.isNullOrBlank()) {
                user.phone = request.phone
            }
            if (!request.location.isNullOrBlank()) {
                user.location = request.location
            }
            if (!request.educationalBackground.isNullOrBlank()) {
                user.educationalBackground = request.educationalBackground
            }
            if (!request.description.isNullOrBlank()) {
                user.description = request.description
            }
            if (request.userInfo != null) {
                val userInfo = user.userInfo.to<UserInfoDTO>()
                ReflectUtils.copyPropertyNonNull(
                    SchoolInfoDTO::class,
                    request.userInfo.schoolInfo,
                    userInfo.schoolInfo
                )
                ReflectUtils.copyPropertyNonNull(
                    SocialInfoDTO::class,
                    request.userInfo.socialInfo,
                    userInfo.socialInfo
                )
                user.userInfo = userInfo.toJSONString()
            }
            if (request.isAdmin != null) {
                user.isAdmin = request.isAdmin
            }
            userRepository.save(user)
            Response.success<Unit>("????????????")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.userId.get()}"
                return Response.failure("???????????????????????????????????????token????????????, ??????????????????")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("????????????, ??????????????????"))
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
            if (t != null) return Response.failure("?????????????????????")
            val (re, msg) = verifyCodeCheck(verifyCode)
            if (!re && msg != null) return@runCatching msg
            if (redisUtils["email"] != email) return Response.failure("?????????????????????????????????")
            if (MD5Util.encode(password) != user.password) return Response.failure("????????????")
            val (code, html) = getHtml("http://localhost:8090/change_email?email=${email}")
            val success =
                if (code == 200 && html != null) sendEmail(
                    user.email,
                    "BUAA????????????????????????????????????",
                    html
                ) else false
            if (!success) throw Exception("??????????????????")
            user.email = email
            userRepository.save(user)
            Response.success("????????????")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.userId.get()}"
                return Response.failure("???????????????????????????????????????token????????????, ??????????????????")
            }
            if (it.message != null) return Response.failure("${it.message}")
            else log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("????????????, ??????????????????"))
    }

    override fun queryHeaders(): Response<List<ColumnDSL>> {
        val templates = tableTemplateRepository.findByTemplateType(TemplateType.USER_LIST_TEMPLATE)
        if (templates.isEmpty()) {
            log.error("[UserServiceImpl.queryHeaders] ????????????????????????")
            return Response.failure(ExceptionEnum.TEMPLATE_NOT_EXIST)
        }
        return Response.success(templates[ICConstant.INT_0].templateInfo.parseArray<ColumnDSL>())
    }

    override fun list(
        realName: String?,
        gender: String?,
        birthday: String?,
        location: String?,
        studentId: String?,
        classId: String?,
        admissionYear: String?,
        graduationYear: String?,
        college: String?,
        industry: String?,
        company: String?,
        page: Int,
        limit: Int
    ): Response<ListUserResponse> {
        val specification = Specification<User> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!realName.isNullOrBlank()) {
                predicates += cb.like(root.get("realName"), "%$realName%")
            }
            if (!gender.isNullOrBlank()) {
                val g = User.Gender.getByDesc(gender).code
                predicates += cb.equal(root.get<Int>("gender"), g)
            }
            if (!birthday.isNullOrBlank()) {
                val b = LocalDate.parse(birthday)
                predicates += cb.equal(root.get<LocalDate>("birthday"), b)
            }
            if (!location.isNullOrBlank()) {
                predicates += cb.like(root.get("location"), "%$location%")
            }
            if (!studentId.isNullOrBlank()) {
                predicates += cb.like(root.get("userInfo"), "%\"studentId\":\"$studentId\"%")
            }
            if (!classId.isNullOrBlank()) {
                predicates += cb.like(root.get("userInfo"), "%\"classId\":\"$classId\"%")
            }
            if (!admissionYear.isNullOrBlank()) {
                predicates += cb.like(root.get("userInfo"), "%\"admissionYear\":\"$admissionYear\"%")
            }
            if (!graduationYear.isNullOrBlank()) {
                predicates += cb.like(root.get("userInfo"), "%\"graduationYear\":\"$graduationYear\"%")
            }
            if (!college.isNullOrBlank()) {
                predicates += cb.like(root.get("userInfo"), "%\"college\":\"$college\"%")
            }
            if (!industry.isNullOrBlank()) {
                predicates += cb.like(root.get("userInfo"), "%\"industry\":\"$industry\"%")
            }
            if (!company.isNullOrBlank()) {
                predicates += cb.like(root.get("userInfo"), "%\"company\":\"$company\"%")
            }
            predicates += cb.equal(root.get<Boolean>("isDeleted"), false)
            query.where(*predicates.toTypedArray()).restriction
        }
        val result = userRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val users = result.content.map { it.toDTO() }
        return Response.success(ListUserResponse(total, users))
    }

    override fun download(): XSSFWorkbook {
        val headers = queryHeaders().data ?: run {
            log.error("[UserServiceImpl.download] ????????????????????????")
            throw ServiceException(ExceptionEnum.TEMPLATE_NOT_EXIST)
        }
        val data = userRepository.findAll().map { it.toDTO() }
        return XSSFWorkbook().process(headers, data)
    }

    override fun downloadTemplate(): XSSFWorkbook {
        val excelSheetDefinitions = getExcelSheetDefinitions()
        val user = UserDTO(
            email = "123456789@qq.com",
            username = "123456",
            realName = "??????",
            gender = "???",
            phone = "12345678999",
            birthday = LocalDate.now(),
            studentId = "12345678",
            admissionYear = "2014",
            graduationYear = "2018",
            userInfo = UserInfoDTO(SchoolInfoDTO(), SocialInfoDTO())
        )
        return XSSFWorkbook().getTemplate(excelSheetDefinitions, listOf(user))
    }

    private fun getExcelSheetDefinitions(): MutableList<ExcelSheetDefinition> {
        val templates = tableTemplateRepository.findByTemplateType(TemplateType.USER_UPLOAD_TEMPLATE)
        if (templates.isEmpty()) {
            log.error("[UserServiceImpl.downloadTemplate] ????????????????????????")
            throw ServiceException(ExceptionEnum.TEMPLATE_NOT_EXIST)
        }
        return templates[ICConstant.INT_0].templateInfo.parseArray<ExcelSheetDefinition>()
    }

    override fun upload(file: MultipartFile): Response<Unit> {
        val excelSheetDefinitions = getExcelSheetDefinitions()
        val users = file.read(excelSheetDefinitions, UserUploadConverter::convert)
        userRepository.saveAll(users)
        return Response.success("????????????")
    }
}