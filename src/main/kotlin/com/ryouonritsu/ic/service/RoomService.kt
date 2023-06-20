package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.domain.dto.RoomDTO
import com.ryouonritsu.ic.domain.protocol.request.ModifyRoomInfoRequest
import com.ryouonritsu.ic.domain.protocol.response.ListRoomResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

/**
 * @author PaulManstein
 */
interface RoomService {
    fun selectRoomById(roomId: Long): Response<RoomDTO>
    fun modifyRoomInfo(request: ModifyRoomInfoRequest): Response<Unit>
    fun queryHeaders(): Response<List<ColumnDSL>>
    fun list(
        id: Long?,
        userId: Long?,
        status: Boolean?,
        commence: LocalDate?,
        terminate: LocalDate?,
        contract: Long?,
        roomInfo: String?,
        page: Int,
        limit: Int
    ): Response<ListRoomResponse>

    fun download(): XSSFWorkbook
    fun downloadTemplate(): XSSFWorkbook
    fun upload(file: MultipartFile): Response<Unit>
//    fun findByKeyword(keyword: String): Response<List<RoomDTO>>
}