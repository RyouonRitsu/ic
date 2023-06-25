package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.domain.dto.UserDTO
import com.ryouonritsu.ic.domain.protocol.request.*
import com.ryouonritsu.ic.domain.protocol.response.ListUserResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile

/**
 * @author ryouonritsu
 */
interface UserService {
    fun sendSmsVerificationCode(phone: String): Response<Unit>
    fun sendRegistrationVerificationCode(email: String?, modify: Boolean): Response<Unit>
    fun generateInvitationCode(): Response<String>
    fun register(request: RegisterRequest): Response<Unit>
    fun addSingleUser(request: AddSingleUserRequest): Response<Unit>
    fun login(request: LoginRequest): Response<Map<String, Any>>
    fun showInfo(userId: Long): Response<UserDTO>
    fun sendForgotPasswordEmail(email: String?): Response<Unit>
    fun changePassword(mode: Int, request: ChangePasswordRequest): Response<Unit>
    fun uploadFile(file: MultipartFile): Response<List<Map<String, String>>>
    fun deleteFile(url: String): Response<Unit>
    fun modifyUserInfo(request: ModifyUserInfoRequest): Response<Unit>
    fun modifyEmail(request: ModifyEmailRequest): Response<Unit>
    fun queryHeaders(): Response<List<ColumnDSL>>
    fun list(
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
    ): Response<ListUserResponse>

    fun download(
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
    ): XSSFWorkbook

    fun downloadTemplate(): XSSFWorkbook
    fun upload(file: MultipartFile): Response<Unit>
    fun findByKeyword(keyword: String): Response<List<UserDTO>>
}