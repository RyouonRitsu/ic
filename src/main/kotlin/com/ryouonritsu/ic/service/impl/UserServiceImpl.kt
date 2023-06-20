package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.constants.ICConstant.INT_0
import com.ryouonritsu.ic.common.constants.ICConstant.INT_1
import com.ryouonritsu.ic.common.constants.ICConstant.INT_20000
import com.ryouonritsu.ic.common.constants.TemplateType
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.MD5Util
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.common.utils.TokenUtils
import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.component.file.ExcelSheetDefinition
import com.ryouonritsu.ic.component.file.converter.UserUploadConverter
import com.ryouonritsu.ic.component.getTemplate
import com.ryouonritsu.ic.component.process
import com.ryouonritsu.ic.component.read
import com.ryouonritsu.ic.domain.dto.UserDTO
import com.ryouonritsu.ic.domain.protocol.request.*
import com.ryouonritsu.ic.domain.protocol.response.ListUserResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.InvitationCode
import com.ryouonritsu.ic.entity.User
import com.ryouonritsu.ic.entity.UserFile
import com.ryouonritsu.ic.manager.db.UserManager
import com.ryouonritsu.ic.repository.InvitationCodeRepository
import com.ryouonritsu.ic.repository.UserFileRepository
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.TableTemplateService
import com.ryouonritsu.ic.service.UserService
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.criteria.Predicate
import kotlin.io.path.Path

/**
 * @author ryouonritsu
 */
@Service
class UserServiceImpl(
    private val redisUtils: RedisUtils,
    private val userManager: UserManager,
    private val userRepository: UserRepository,
    private val userFileRepository: UserFileRepository,
    private val invitationCodeRepository: InvitationCodeRepository,
    private val tableTemplateService: TableTemplateService,
    private val transactionTemplate: TransactionTemplate,
    @Value("\${static.file.prefix}")
    private val staticFilePrefix: String,
    @Value("\${server.port}")
    private val serverPort: Int,
    @Value("\${mail.text.change-email}")
    private val mailTextChangeEmail: String,
    @Value("\${mail.text.change-email.notice}")
    private val mailTextChangeEmailNotice: String,
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

    private fun check(
        email: String,
        username: String,
        password: String,
        realName: String
    ): Pair<Boolean, Response<Unit>?> {
        if (!email.matches(Regex("[\\w\\\\.]+@[\\w\\\\.]+\\.\\w+"))) return Pair(
            false, Response.failure("邮箱格式不正确")
        )
        if (username.length > 255) return Pair(
            false, Response.failure("用户名长度不能超过255")
        )
        if (password.length > 255) return Pair(
            false, Response.failure("密码长度不能超过255")
        )
        if (realName.length > 255) return Pair(
            false, Response.failure("真实姓名长度不能超过255")
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
        val (code, html) = getHtml("http://localhost:$serverPort/$template?verification_code=$verificationCode")
        val success = if (code == 200 && html != null) userManager.sendEmail(email, subject, html)
        else false
        return if (success) {
            redisUtils.set(email, verificationCode, 5, TimeUnit.MINUTES)
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
        val subject = if (modify) mailTextChangeEmail else mailTextRegister
        val verificationCode = (1..6).joinToString("") { "${(0..9).random()}" }
        return sendVerifyCodeEmailUseTemplate(
            "registration_verification",
            verificationCode,
            email,
            subject
        )
    }

    override fun generateInvitationCode(): Response<String> {
        val codes = invitationCodeRepository.findAllByUserIdAndStatus(RequestContext.user!!.id)
        if (codes.isNotEmpty())
            return Response.success(codes[INT_0].code)

        val code = InvitationCode(
            userId = RequestContext.user!!.id,
            code = UUID.randomUUID().toString().replace("-", "")
        )
        transactionTemplate.execute { invitationCodeRepository.save(code) }
        return Response.success(code.code)
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun register(request: RegisterRequest): Response<Unit> {
        val (result, message) = check(
            request.email!!, request.username!!, request.password1!!, request.legalName!!
        )
        if (!result && message != null) return message
        val t = userRepository.findByEmail(request.email)
        if (t != null) return Response.failure("该邮箱已被注册")
        val codes = invitationCodeRepository.findAllByCodeAndStatus(request.invitationCode!!)
        if (codes.isEmpty()) return Response.failure("邀请码无效")
        val (re, msg) = userManager.verifyCodeCheck(request.email, request.verificationCode)
        if (!re && msg != null) return msg
        if (redisUtils[request.email].isNullOrBlank())
            return Response.failure("该邮箱与验证邮箱不匹配")
        val temp = userRepository.findByIdentifier(request.username)
        if (temp != null) return Response.failure("用户名已存在")
        if (request.password1 != request.password2)
            return Response.failure("两次输入的密码不一致")
        userRepository.save(
            User(
                email = request.email,
                username = request.username,
                password = MD5Util.encode(request.password1),
                avatar = request.avatar,
                legalName = request.legalName,
                userType = User.UserType.ADMIN()
            )
        )
        return Response.success("注册成功")
    }

    override fun login(request: LoginRequest): Response<Map<String, Any>> {
        val user = userRepository.findByIdentifier(request.identifier!!)
            ?: throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        if (MD5Util.encode(request.password) != user.password)
            return Response.failure("密码错误")
        val token = TokenUtils.sign(user)
        if (request.keepLogin) redisUtils["${user.id}"] = token
        else redisUtils.set("${user.id}", token, 3, TimeUnit.DAYS)
        return Response.success(
            "登录成功", mapOf(
                "token" to token,
                "user" to user.toDTO()
            )
        )
    }

    override fun showInfo(userId: Long): Response<UserDTO> {
        return runCatching {
            val user = userRepository.findById(userId).get()
            Response.success("获取成功", user.toDTO())
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

    override fun sendForgotPasswordEmail(email: String?): Response<Unit> {
        val (result, message) = emailCheck(email)
        if (!result && message != null) return message
        userRepository.findByEmail(email!!) ?: return Response.failure("该邮箱未被注册")
        val subject = mailTextForgetPassword
        val verificationCode = (1..6).joinToString("") { "${(0..9).random()}" }
        return sendVerifyCodeEmailUseTemplate(
            "forgot_password",
            verificationCode,
            email,
            subject
        )
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun changePassword(mode: Int, request: ChangePasswordRequest): Response<Unit> {
        when (mode) {
            0 -> {
                val (re, msg) = emailCheck(request.email)
                if (!re && msg != null) return msg
                val (result, message) = userManager.verifyCodeCheck(
                    request.email!!, request.verifyCode
                )
                if (!result && message != null) return message
                if (request.password1 != request.password2) return Response.failure("两次密码不一致")
                return runCatching {
                    val user = userRepository.findByEmail(request.email)
                        ?: return Response.failure("该邮箱未被注册, 发生意外错误, 请检查数据库")
                    user.password = MD5Util.encode(request.password1)
                    userRepository.save(user)
                    Response.success<Unit>("修改成功")
                }.onFailure { log.error(it.stackTraceToString()) }
                    .getOrDefault(Response.failure("修改失败, 发生意外错误"))
            }

            1 -> {
                return runCatching {
                    val user = userRepository.findById(
                        RequestContext.user?.id
                            ?: return Response.failure("无法验证用户信息, 请登录!")
                    ).get()
                    if (MD5Util.encode(request.oldPassword) != user.password)
                        return Response.failure("原密码错误")
                    if (request.password1!!.length < 8 || request.password1.length > 30)
                        return Response.failure("密码长度必须在8-30位之间")
                    if (request.password1 != request.password2)
                        return Response.failure("两次密码不一致")
                    user.password = MD5Util.encode(request.password1)
                    userRepository.save(user)
                    Response.success<Unit>("修改成功")
                }.onFailure {
                    if (it is NoSuchElementException) {
                        redisUtils - "${RequestContext.user!!.id}"
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

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun uploadFile(
        file: MultipartFile
    ): Response<List<Map<String, String>>> {
        return runCatching {
            if (file.size >= 10 * 1024 * 1024) return Response.failure("上传失败, 文件大小超过最大限制10MB！")
            val time = System.currentTimeMillis()
            val userId = RequestContext.user?.id
            val fileDir = "static/file/${userId}"
            val fileName = "${time}_${file.originalFilename}"
            val filePath = "$fileDir/$fileName"
            if (!File(fileDir).exists()) File(fileDir).mkdirs()
            file.transferTo(Path(filePath))
            val fileUrl = "http://$staticFilePrefix:$serverPort/file/${userId}/${fileName}"
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

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun deleteFile(url: String): Response<Unit> {
        return try {
            val file = userFileRepository.findByUrl(url)
                ?: return Response.failure("文件不存在")
            File(file.filePath).delete()
            userFileRepository.delete(file)
            Response.failure("删除成功")
        } catch (e: Exception) {
            log.error(e.stackTraceToString())
            Response.failure("删除失败, 发生意外错误")
        }
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun modifyUserInfo(request: ModifyUserInfoRequest): Response<Unit> {
        return runCatching {
            val user = userRepository.findById(request.id ?: RequestContext.user!!.id).get()
            if (!request.email.isNullOrBlank()) user.email = request.email!!
            if (!request.username.isNullOrBlank()) {
                val t = userRepository.findByIdentifier(request.username)
                if (t != null) return Response.failure("用户名已存在")
                if (request.username.length > 50) return Response.failure("用户名长度不能超过50")
                user.username = request.username
            }
            if (!request.avatar.isNullOrBlank()) user.avatar = request.avatar
            if (!request.legalName.isNullOrBlank()) user.legalName = request.legalName
            if (!request.gender.isNullOrBlank()) user.gender =
                User.Gender.getByDesc(request.gender).code
            if (!request.birthday.isNullOrBlank()) {
                try {
                    user.birthday =
                        LocalDate.parse(request.birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    return Response.failure("生日格式错误, 应为yyyy-MM-dd")
                }
            }
            if (!request.contactName.isNullOrBlank()) user.contactName = request.contactName
            if (!request.phone.isNullOrBlank()) user.phone = request.phone
            if (!request.location.isNullOrBlank()) user.location = request.location
            if (!request.companyName.isNullOrBlank()) user.companyName = request.companyName
            if (!request.position.isNullOrBlank()) user.position = request.position
            userRepository.save(user)
            Response.success<Unit>("修改成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("修改失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun modifyEmail(request: ModifyEmailRequest): Response<Unit> {
        return runCatching {
            val user = userRepository.findById(RequestContext.user!!.id).get()
            val (result, message) = emailCheck(request.email)
            if (!result && message != null) return message
            val t = userRepository.findByEmail(request.email!!)
            if (t != null) return Response.failure("该邮箱已被注册")
            val (re, msg) = userManager.verifyCodeCheck(request.email, request.verifyCode)
            if (!re && msg != null) return@runCatching msg
            if (redisUtils[request.email].isNullOrBlank())
                return Response.failure("该邮箱与验证邮箱不匹配")
            if (MD5Util.encode(request.password) != user.password) return Response.failure("密码错误")
            val (code, html) = getHtml("http://localhost:$serverPort/change_email?email=${request.email}")
            val success =
                if (code == 200 && html != null) userManager.sendEmail(
                    user.email,
                    mailTextChangeEmailNotice,
                    html
                ) else false
            if (!success) throw Exception("邮件发送失败")
            user.email = request.email
            userRepository.save(user)
            Response.success("修改成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            if (it.message != null) return Response.failure("${it.message}")
            else log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("修改失败, 发生意外错误"))
    }

    override fun queryHeaders(): Response<List<ColumnDSL>> {
        return Response.success(tableTemplateService.queryHeaders(TemplateType.USER_LIST_TEMPLATE))
    }

    override fun list(
        ids: List<Long>?,
        username: String?,
        legalName: String?,
        gender: Int?,
        contactName: String?,
        phone: String?,
        location: String?,
        companyName: String?,
        position: String?,
        userType: Int?,
        page: Int,
        limit: Int
    ): Response<ListUserResponse> {
        val specification = Specification<User> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!ids.isNullOrEmpty()) predicates += cb.`in`(root.get<Long>("id")).apply {
                ids.forEach { this.value(it) }
            }
            if (!username.isNullOrBlank()) predicates += cb.like(root["username"], "%$username%")
            if (!legalName.isNullOrBlank()) predicates += cb.like(root["legalName"], "%$legalName%")
            if (gender != null) predicates += cb.equal(root.get<Int>("gender"), gender)
            if (!contactName.isNullOrBlank())
                predicates += cb.like(root["contactName"], "%$contactName%")
            if (!phone.isNullOrBlank()) predicates += cb.equal(root.get<String>("phone"), phone)
            if (!location.isNullOrBlank())
                predicates += cb.equal(root.get<String>("location"), location)
            if (!companyName.isNullOrBlank())
                predicates += cb.like(root["companyName"], "%$companyName%")
            if (!position.isNullOrBlank()) predicates += cb.like(root["position"], "%$position%")
            if (userType != null) predicates += cb.equal(root.get<Int>("userType"), userType)
            predicates += cb.equal(root.get<Boolean>("status"), true)
            query.where(*predicates.toTypedArray())
                .orderBy(cb.asc(root.get<Long>("id")))
                .restriction
        }
        val result = userRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val users = result.content.map { it.toDTO() }
        return Response.success(ListUserResponse(total, users))
    }

    override fun download(
        ids: List<Long>?,
        username: String?,
        legalName: String?,
        gender: Int?,
        contactName: String?,
        phone: String?,
        location: String?,
        companyName: String?,
        position: String?,
        userType: Int?
    ): XSSFWorkbook {
        val headers = queryHeaders().data ?: run {
            log.error("[UserServiceImpl.download] 没有用户列表模板")
            throw ServiceException(ExceptionEnum.TEMPLATE_NOT_EXIST)
        }
        val data = list(
            ids, username, legalName, gender,
            contactName, phone, location, companyName,
            position, userType, INT_1, INT_20000
        ).data?.list ?: run {
            log.error("[UserServiceImpl.download] 响应数据为空")
            listOf()
        }
        return XSSFWorkbook().process(headers, data)
    }

    override fun downloadTemplate(): XSSFWorkbook {
        val excelSheetDefinitions = getExcelSheetDefinitions()
        val user = UserDTO(
            email = "123456789@qq.com",
            username = "123456",
            legalName = "张三",
            contactName = "李四",
            phone = "12345678999",
            companyName = "BD",
            position = "普通客户",
            userType = "0"
        )
        return XSSFWorkbook().getTemplate(excelSheetDefinitions, listOf(user))
    }

    private fun getExcelSheetDefinitions(): List<ExcelSheetDefinition> {
        return tableTemplateService.queryExcelSheetDefinitions(TemplateType.USER_UPLOAD_TEMPLATE)
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun upload(file: MultipartFile): Response<Unit> {
        val excelSheetDefinitions = getExcelSheetDefinitions()
        val users = file.read(excelSheetDefinitions, UserUploadConverter::convert)
        val identifierList = users.map { it.username } + users.map { it.email }
        if (userRepository.findByIdentifierList(identifierList).isNotEmpty())
            throw ServiceException(ExceptionEnum.DATA_CONFLICT)
        userRepository.saveAll(users)
        return Response.success("上传成功")
    }

    override fun findByKeyword(keyword: String): Response<List<UserDTO>> {
        val users = userRepository.findByKeyword(keyword)
        return Response.success(users.map { it.toDTO() })
    }
}