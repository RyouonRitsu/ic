package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.RoomDTO
import javax.xml.ws.Response

/**
 * @author PaulManstein
 */
interface RoomService {
    fun shouInfo(roomId:Long): Response<List<RoomDTO>>
}