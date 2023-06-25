package com.ryouonritsu.ic.domain.protocol.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author ryouonritsu
 */
@Schema(description = "未读通知响应")
data class UnreadNotificationResponse(
    @Schema(description = "是否有未读通知", required = true)
    val hasUnreadNotifications: Boolean,
    @Schema(description = "未读通知计数", required = true)
    val unreadNotificationCount: Long
)
