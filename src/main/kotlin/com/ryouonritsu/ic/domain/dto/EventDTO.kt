package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * @author ryouonritsu
 */
@Schema(description = "Event entity")
data class EventDTO(
    @Schema(description = "事件ID", example = "1", required = true)
    var id: String = "0",
    @Schema(description = "订阅标签", example = "tag", required = true)
    var tag: String,
    @Schema(description = "事件名", example = "name", required = true)
    var name: String = "",
    @Schema(description = "事件内容", example = "message", required = true)
    var message: String = "",
    @Schema(description = "创建时间", example = "2018-11-23T09:45:45.000+08:00", required = true)
    var createTime: LocalDateTime = LocalDateTime.now(),
    @Schema(description = "修改时间", example = "2018-11-23T09:45:45.000+08:00", required = true)
    var modifyTime: LocalDateTime = LocalDateTime.now(),
)
