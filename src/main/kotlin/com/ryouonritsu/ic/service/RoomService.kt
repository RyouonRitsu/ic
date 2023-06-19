package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.domain.dto.RoomDTO
import com.ryouonritsu.ic.domain.protocol.response.Response
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import javax.print.DocFlavor.STRING

/**
 * @author PaulManstein
 */
interface RoomService {
    fun showInfo(roomId: Long): Response<List<RoomDTO>>
    fun selectRoomById(roomId: Long): Response<List<RoomDTO>>
    fun uploadFile(file: MultipartFile): Response<List<Map<String, String>>>
//    fun deleteFile(url: String): Response<Unit>
//    fun modifyRoomInfo(request: ModifyRoomInfoRequest): Response<Unit>
    fun queryHeaders(): Response<List<ColumnDSL>>
//    fun list()
    fun download(): XSSFWorkbook
    fun downloadTemplate(): XSSFWorkbook
    fun upload(file: MultipartFile): Response<Unit>
//    fun findByKeyword(keyword: String): Response<List<RoomDTO>>
}