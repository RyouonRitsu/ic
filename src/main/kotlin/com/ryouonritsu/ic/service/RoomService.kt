package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.domain.dto.RoomDTO
import com.ryouonritsu.ic.domain.protocol.response.Response
import org.springframework.web.multipart.MultipartFile

/**
 * @author PaulManstein
 */
interface RoomService {
    fun showInfo(roomId: Long): Response<List<RoomDTO>>
    fun selectRoomById(roomId: Long): Response<List<RoomDTO>>
    fun uploadFile(file: MultipartFile): Response<List<Map<String, String>>>
}