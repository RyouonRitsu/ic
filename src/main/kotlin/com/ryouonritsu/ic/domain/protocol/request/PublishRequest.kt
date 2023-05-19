package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "发布事件请求")
data class PublishRequest(
    @field:NotBlank
    @Schema(description = "标签", example = "error", required = true)
    val tag: String?,
    @Schema(description = "名称", example = "log")
    val name: String?,
    @field:NotBlank
    @Schema(description = "消息", example = "Something wrong", required = true)
    val message: String?,
)
