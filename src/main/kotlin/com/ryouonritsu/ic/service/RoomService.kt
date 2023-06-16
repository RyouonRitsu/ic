package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.RoomDTO
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @author PaulManstein
 */
interface RoomService {
    fun showInfo(roomId: Long): Response<List<RoomDTO>>
    fun selectRoomById(roomId: Long): Response<List<RoomDTO>>
}