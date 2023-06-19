package com.ryouonritsu.ic.domain.protocol.response

import com.ryouonritsu.ic.domain.dto.RoomDTO
import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author PaulManstein
 */
@Schema(description = "房间列表查询相应")
class ListRoomResponse(
    @Schema(description = "总数", required = true)
    val total: Long,
    @Schema(description = "房间列表", required = true)
    val list: List<RoomDTO>
)