package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.service.RoomService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * @author PaulManstein
 */
@Validated
@RestController
@RequestMapping("/room")
@Tag(name = "房间接口")
class RoomController(
    private val roomService: RoomService,
    private val redisUtils: RedisUtils
) {
    companion object{
        private val log = LoggerFactory.getLogger(RoomController::class.java)
    }

    @ServiceLog(description = "根据房间ID查询房间信息")
    @GetMapping("/selectRoomByRoomId")
    @Tag(name = "房间接口")
    @Operation(summary = "根据房间ID查询房间信息")
    fun selectRoomByRoomId(
        @RequestParam("room_id") @Parameter(
            description = "房间id",
            required = true
        ) roomId: Long
    ) = roomService.selectRoomById(roomId)

}