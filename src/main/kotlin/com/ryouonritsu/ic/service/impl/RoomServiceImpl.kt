package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RedisUtils.Companion.log
import com.ryouonritsu.ic.domain.dto.RoomDTO
import com.ryouonritsu.ic.repository.RoomRepository
import com.ryouonritsu.ic.service.RoomService
import org.springframework.stereotype.Service
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @author PaulManstein
 */

@Service
class RoomServiceImpl(
    private val redisUtils: RedisUtils,
    private val roomRepository: RoomRepository,
) : RoomService {
    override fun showInfo(roomId: Long): Response<List<RoomDTO>> {
        TODO("Not yet implemented")
    }
    override fun selectRoomById(roomId: Long): Response<List<RoomDTO>> {
        return runCatching {
            val room = roomRepository.findById(roomId).get()
            Response.success("获取成功", listOf(room.toDTO()))
        }.onFailure {
            if(it is NoSuchElementException) return Response.failure("数据库中没有此房间")
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("获取失败，发生意外错误"))
    }
}