package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * @Author Kude
 * @Date 2023/6/26 10:20
 */
@Schema(description = "Visitor entity")
data class VisitorDTO(
    @Schema(description = "访客ID", example = "1", required = true)
    var id: String = "",
    @Schema(description = "邀请人ID", example = "1", required = true)
    var customId: String = "1",
    @Schema(description = "访客人员姓名", example = "张三", required = true)
    var visitorName: String = "",
    @Schema(description = "身份证号码", example = "ID card number", required = true)
    var cardNumber: String = "",
    @Schema(description = "电话号码", example = "13939393939", required = true)
    var phoneNumber: String = "",
    @Schema(description = "到访时间", example = "2023-06-25 10:00:00", required = true)
    var visitTime: LocalDateTime,
    @Schema(description = "访问状态", example = "0", required = true)
    var visitStatus: String = "0",
    @Schema(description = "创建时间", example = "2023-06-16 18:19:22", required = true)
    var createTime: LocalDateTime,
    @Schema(description = "最后修改时间", example = "2023-06-16 18:19:22", required = true)
    var modifyTime: LocalDateTime,
)