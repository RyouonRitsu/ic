package com.ryouonritsu.ic.domain.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "Subscriber entity")
data class SubscriberDTO(
    @Schema(description = "订阅者ID", example = "1", required = true)
    var id: String = "0",
    @Schema(description = "订阅标签", example = "tag", required = true)
    var tag: String,
    @Schema(description = "订阅邮箱", example = "email@example.com", required = true)
    var email: String,
)
