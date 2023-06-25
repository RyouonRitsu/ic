package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.constants.ICConstant.INT_0
import com.ryouonritsu.ic.common.constants.ICConstant.INT_1
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.common.utils.DownloadUtils
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.protocol.request.*
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.User
import com.ryouonritsu.ic.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Validated
@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
class UserController(
    private val userService: UserService,
    private val redisUtils: RedisUtils
) {
    companion object {
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }

    @ServiceLog(description = "发送手机验证码")
    @PostMapping("/sendSmsVerificationCode")
    @Tag(name = "用户接口")
    @Operation(summary = "发送手机验证码", description = "发送手机验证码")
    fun sendSmsVerificationCode(@RequestBody @Valid request: SendSmsRequest) =
        userService.sendSmsVerificationCode(request.phone!!)

    @ServiceLog(description = "发送注册验证码")
    @PostMapping("/sendRegistrationVerificationCode")
    @Tag(name = "用户接口")
    @Operation(
        summary = "发送注册验证码",
        description = "发送注册验证码到指定邮箱, 若modify为true, 则发送修改邮箱验证码, 默认为false"
    )
    fun sendRegistrationVerificationCode(@RequestBody @Valid request: SendRegistrationVerificationCodeRequest) =
        userService.sendRegistrationVerificationCode(request.email, request.modify)

    @ServiceLog(description = "生成注册邀请码")
    @PostMapping("/generateInvitationCode")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "用户接口")
    @Operation(
        summary = "生成注册邀请码",
        description = "管理员生成注册邀请码"
    )
    fun generateInvitationCode() = userService.generateInvitationCode()

    @ServiceLog(description = "用户注册")
    @PostMapping("/register")
    @Tag(name = "用户接口")
    @Operation(summary = "用户注册", description = "除了真实姓名和头像地址其余必填")
    fun register(@RequestBody @Valid request: RegisterRequest) = userService.register(request)

    @ServiceLog(description = "管理员上传单个用户信息")
    @PostMapping("/addSingleUser")
    @Tag(name = "管理员专用接口")
    @Operation(summary = "添加单个用户", description = "")
    fun addSingleUser(@RequestBody @Valid request: AddSingleUserRequest) =
        userService.addSingleUser(request)

    @ServiceLog(description = "用户登录")
    @PostMapping("/login")
    @Tag(name = "用户接口")
    @Operation(
        summary = "用户登录",
        description = "keep_login为true时, 保持登录状态, 否则token会在3天后失效, 默认为false"
    )
    fun login(@RequestBody @Valid request: LoginRequest) = userService.login(request)

    @ServiceLog(description = "用户登出")
    @GetMapping("/logout")
    @AuthCheck
    @Tag(name = "用户接口")
    @Operation(summary = "用户登出")
    fun logout(): Response<Any> {
        redisUtils - "${RequestContext.user!!.id}"
        return Response.success("登出成功")
    }

    @ServiceLog(description = "返回已登陆用户的信息")
    @GetMapping("/showInfo")
    @AuthCheck
    @Tag(name = "用户接口")
    @Operation(summary = "返回已登陆用户的信息", description = "需要用户登陆才能查询成功")
    fun showInfo() = userService.showInfo(RequestContext.user!!.id)

    @ServiceLog(description = "根据用户id查询用户信息")
    @GetMapping("/selectUserByUserId")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "用户接口")
    @Operation(summary = "根据用户id查询用户信息")
    fun selectUserByUserId(
        @RequestParam("user_id") @Parameter(
            description = "用户id",
            required = true
        ) userId: Long
    ) = userService.showInfo(userId)

    @ServiceLog(description = "发送找回密码验证码")
    @PostMapping("/sendForgotPasswordEmail")
    @Tag(name = "用户接口")
    @Operation(summary = "发送找回密码验证码", description = "发送找回密码验证码到指定邮箱")
    fun sendForgotPasswordEmail(@RequestBody @Valid request: SendForgotPasswordEmailRequest) =
        userService.sendForgotPasswordEmail(request.email)

    @ServiceLog(description = "通过邮箱修改用户密码")
    @PostMapping("/changePasswordByEmail")
    @Tag(name = "用户接口")
    @Operation(
        summary = "通过邮箱修改用户密码",
        description = "需要提供邮箱, 验证码, 新密码和确认密码"
    )
    fun changePasswordByEmail(@RequestBody @Valid request: ChangePasswordRequest) =
        userService.changePassword(INT_0, request)

    @ServiceLog(description = "通过原密码修改用户密码")
    @PostMapping("/changePasswordByOldPassword")
    @AuthCheck
    @Tag(name = "用户接口")
    @Operation(
        summary = "通过原密码修改用户密码",
        description = "需要提供原密码, 新密码和确认密码"
    )
    fun changePasswordByOldPassword(@RequestBody @Valid request: ChangePasswordRequest) =
        userService.changePassword(INT_1, request)

    @ServiceLog(description = "上传文件", printRequest = false)
    @PostMapping("/uploadFile")
    @AuthCheck
    @Tag(name = "用户接口")
    @Operation(
        summary = "上传文件",
        description = "将用户上传的文件保存在静态文件目录static/file/\${user_id}/\${file_name}下"
    )
    fun uploadFile(@ModelAttribute @Valid request: UserUploadRequest) =
        userService.uploadFile(request.file!!)

    @ServiceLog(description = "删除文件")
    @PostMapping("/deleteFile")
    @AuthCheck
    @Tag(name = "用户接口")
    @Operation(
        summary = "删除文件",
        description = "删除用户上传的文件, 使分享链接失效"
    )
    fun deleteFile(@RequestBody @Valid request: DeleteFileRequest) =
        userService.deleteFile(request.url!!)

    @ServiceLog(description = "修改用户信息")
    @PostMapping("/modifyUserInfo")
    @AuthCheck
    @Tag(name = "用户接口")
    @Operation(
        summary = "修改用户信息",
        description = "未填写的信息则保持原样不变，注意：此接口无法设置\"管理员可用\"字段"
    )
    fun modifyUserInfo(@RequestBody @Valid request: ModifyUserInfoRequest): Response<Unit> {
        request.id = null
        request.email = null
        request.status = null
        return userService.modifyUserInfo(request)
    }

    @ServiceLog(description = "修改邮箱")
    @PostMapping("/modifyEmail")
    @AuthCheck
    @Tag(name = "用户接口")
    @Operation(
        summary = "修改邮箱",
        description = "需要进行新邮箱验证和密码验证, 新邮箱验证发送验证码使用注册验证码接口即可"
    )
    fun modifyEmail(@RequestBody @Valid request: ModifyEmailRequest) =
        userService.modifyEmail(request)

    @ServiceLog(description = "查询用户列表表头")
    @GetMapping("/queryHeaders")
    @AuthCheck
    @Tag(name = "用户接口")
    @Operation(
        summary = "查询用户列表表头",
        description = "查询用户列表表头"
    )
    fun queryHeaders() = userService.queryHeaders()

    @ServiceLog(description = "查询用户列表")
    @GetMapping("/list")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "用户接口")
    @Operation(
        summary = "查询用户列表",
        description = "查询用户列表"
    )
    fun list(
        @RequestParam(
            "ids",
            required = false
        ) @Parameter(description = "id集合，如1,2,3，精确") ids: List<Long>?,
        @RequestParam(
            "username",
            required = false
        ) @Parameter(description = "用户名，模糊") username: String?,
        @RequestParam(
            "legalName",
            required = false
        ) @Parameter(description = "法人名，模糊") legalName: String?,
        @RequestParam(
            "gender",
            required = false
        ) @Parameter(description = "性别，精确") gender: User.Gender?,
        @RequestParam(
            "contactName",
            required = false
        ) @Parameter(description = "联系人名，模糊") contactName: String?,
        @RequestParam(
            "phone",
            required = false
        ) @Parameter(description = "手机号，精确") phone: String?,
        @RequestParam(
            "location",
            required = false
        ) @Parameter(description = "位置，精确") location: String?,
        @RequestParam(
            "companyName",
            required = false
        ) @Parameter(description = "公司名，模糊") companyName: String?,
        @RequestParam(
            "position",
            required = false
        ) @Parameter(description = "职位，模糊") position: String?,
        @RequestParam(
            "userType",
            required = false
        ) @Parameter(description = "用户类型，精确") userType: User.UserType?,
        @RequestParam("page") @Parameter(
            description = "页码, 从1开始",
            required = true
        ) @Valid @NotNull @Min(1) page: Int?,
        @RequestParam("limit") @Parameter(
            description = "每页数量, 大于0",
            required = true
        ) @Valid @NotNull @Min(1) limit: Int?
    ) = userService.list(
        ids, username, legalName, gender?.code,
        contactName, phone, location, companyName,
        position, userType?.code, page!!, limit!!
    )

    @ServiceLog(description = "用户列表下载", printResponse = false)
    @GetMapping("/download")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "用户接口")
    @Operation(
        summary = "用户列表下载",
        description = "用户列表下载"
    )
    fun download(
        @RequestParam(
            "ids",
            required = false
        ) @Parameter(description = "id集合，如1,2,3，精确") ids: List<Long>?,
        @RequestParam(
            "username",
            required = false
        ) @Parameter(description = "用户名，模糊") username: String?,
        @RequestParam(
            "legalName",
            required = false
        ) @Parameter(description = "法人名，模糊") legalName: String?,
        @RequestParam(
            "gender",
            required = false
        ) @Parameter(description = "性别，精确") gender: User.Gender?,
        @RequestParam(
            "contactName",
            required = false
        ) @Parameter(description = "联系人名，模糊") contactName: String?,
        @RequestParam(
            "phone",
            required = false
        ) @Parameter(description = "手机号，精确") phone: String?,
        @RequestParam(
            "location",
            required = false
        ) @Parameter(description = "位置，精确") location: String?,
        @RequestParam(
            "companyName",
            required = false
        ) @Parameter(description = "公司名，模糊") companyName: String?,
        @RequestParam(
            "position",
            required = false
        ) @Parameter(description = "职位，模糊") position: String?,
        @RequestParam(
            "userType",
            required = false
        ) @Parameter(description = "用户类型，精确") userType: User.UserType?
    ): ResponseEntity<ByteArray> {
        try {
            userService.download(
                ids, username, legalName, gender?.code, contactName,
                phone, location, companyName, position, userType?.code
            ).use { workbook ->
                ByteArrayOutputStream().use { os ->
                    workbook.write(os)
                    return DownloadUtils.downloadFile(
                        "user_${LocalDateTime.now()}.xlsx",
                        os.toByteArray()
                    )
                }
            }
        } catch (e: Exception) {
            log.error("[UserController.download] failed to download users info", e)
            throw e
        }
    }

    @ServiceLog(description = "用户上传模板下载", printResponse = false)
    @GetMapping("/downloadTemplate")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "用户接口")
    @Operation(
        summary = "用户上传模板下载",
        description = "用户上传模板下载"
    )
    fun downloadTemplate(): ResponseEntity<ByteArray> {
        try {
            userService.downloadTemplate().use { wb ->
                ByteArrayOutputStream().use { os ->
                    wb.write(os)
                    return DownloadUtils.downloadFile("user_template.xlsx", os.toByteArray())
                }
            }
        } catch (e: Exception) {
            log.error("[UserController.downloadTemplate] failed to download users template", e)
            throw e
        }
    }

    @ServiceLog(description = "管理员上传用户信息", printRequest = false)
    @PostMapping("/upload")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "用户接口")
    @Operation(
        summary = "管理员上传用户信息",
        description = "管理员上传用户信息"
    )
    fun upload(@ModelAttribute @Valid request: UserUploadRequest): Response<Unit> {
        return userService.upload(request.file!!)
    }

    @ServiceLog(description = "根据关键词查询用户信息")
    @GetMapping("/findByKeyword")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "用户接口")
    @Operation(
        summary = "根据关键词查询用户信息",
        description = "根据关键词查询用户信息，最多返回10个"
    )
    fun findByKeyword(
        @RequestParam("keyword") @Parameter(
            description = "关键词",
            required = true
        ) @Valid @NotNull keyword: String?
    ) = userService.findByKeyword(keyword!!)

    @ServiceLog(description = "管理员修改用户信息")
    @PostMapping("/modifyUserInfoAdvanced")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "用户接口")
    @Operation(
        summary = "修改指定用户信息",
        description = "未填写的信息则保持原样不变"
    )
    fun modifyUserInfoAdvanced(@RequestBody @Valid request: ModifyUserInfoRequest) =
        userService.modifyUserInfo(request)
}