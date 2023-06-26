package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * @Author Kude
 * @Date 2023/6/26 11:04
 */
@Schema(description = "创建访客信息")
data class CreateVisitorRequest(
    @field:NotNull
    @field:NotBlank
    @Schema(description = "访客人员姓名", required = true)
    var visitorName: String,
    @field:NotNull
    @field:NotBlank
    @Schema(description = "身份证号码", required = true)
    var cardNumber: String,
    @field:NotNull
    @field:NotBlank
    @Schema(description = "手机号码", required = true)
    var phoneNumber: String,
    @field:NotNull
    @Schema(description = "到访时间", example = "2023-06-28T13:00:00", required = true)
    var visitTime: LocalDateTime,
)