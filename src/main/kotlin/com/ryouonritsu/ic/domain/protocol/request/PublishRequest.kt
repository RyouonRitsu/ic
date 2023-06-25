package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Schema(description = "发布事件请求")
data class PublishRequest(
    @field:NotNull
    @Schema(description = "用户ID", required = true)
    val userId: Long?,
    @Schema(description = "名称", example = "log")
    val name: String?,
    @field:NotBlank
    @Schema(description = "消息", example = "Something wrong", required = true)
    val message: String?,
    @Schema(description = "是否发送邮件通知", required = true)
    val needSendEmail: Boolean = true
)
