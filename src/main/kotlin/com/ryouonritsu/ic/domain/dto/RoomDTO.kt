package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/**
 * @author PaulManstein
 */
@Schema(description = "Room entity")
data class RoomDTO(
    @Schema(description = "房间ID", example = "1", required = true)
    var id: String = "0",
    @Schema(description = "用户ID", example = "1", required = true)
    var userid: Long = 0,
    @Schema(description = "租赁状态", example = "1", required = true)
    var status: Long = 0,
    @Schema(description = "租赁开始日期", example = "1900-01-01", required = true)
    var commence: LocalDate? = null,
    @Schema(description = "租赁结束日期", example = "2000-01-01", required = true)
    var terminate: LocalDate? = null,
    @Schema(description = "合同ID", example = "1", required = true)
    var contract: Long = 0,
    @Schema(description = "房间其他信息", example = "无", required = true)
    var roomInfo: String = "无"
)