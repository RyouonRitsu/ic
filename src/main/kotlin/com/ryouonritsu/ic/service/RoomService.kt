package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.RoomDTO
import javax.xml.ws.Response

/**
 * @author PaulManstein
 */
interface RoomService {
    fun showInfo(roomId: Long): Response<List<RoomDTO>>
    fun selectRoomById(roomId: Long): Response<List<RoomDTO>>
}