package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "发送短信请求")
data class SendSmsRequest(
    @field:NotBlank
    @Schema(description = "手机号")
    val phone: String?
)
