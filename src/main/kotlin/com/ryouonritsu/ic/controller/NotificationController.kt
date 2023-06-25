package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.domain.protocol.request.BatchPublishRequest
import com.ryouonritsu.ic.domain.protocol.request.BulkIdRequest
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Validated
@RestController
@RequestMapping("/notification")
@Tag(name = "通知服务接口")
class NotificationController(
    private val notificationService: NotificationService
) {
    @ServiceLog(description = "发布通知")
    @PostMapping("/publish")
    @Tag(name = "通知服务接口")
    @Operation(
        summary = "发布通知",
        description = "发布通知, 会尝试发送通知到所有订阅者, 当全部发送失败或无订阅者时, 该通知将会进入pending状态, 等待有人(重新)订阅它"
    )
    fun publish(@Valid @RequestBody request: PublishRequest) = notificationService.publish(request)

    @ServiceLog(description = "批量发布通知")
    @PostMapping("/batchPublish")
    @Tag(name = "通知服务接口")
    @Operation(summary = "批量发布通知", description = "批量发布通知")
    fun batchPublish(@Valid @RequestBody request: BatchPublishRequest) =
        notificationService.batchPublish(request)

    @ServiceLog(description = "批量已读通知")
    @PostMapping("/batchRead")
    @Tag(name = "通知服务接口")
    @Operation(summary = "批量已读通知", description = "批量已读通知")
    fun batchRead(@Valid @RequestBody request: BulkIdRequest) =
        notificationService.batchRead(request)

    @ServiceLog(description = "查询已登陆用户的通知")
    @AuthCheck
    @GetMapping("/list")
    @Tag(name = "通知服务接口")
    @Operation(
        summary = "查询已登陆用户的通知",
        description = "查询过后的通知会被标记为已读"
    )
    fun list(
        @RequestParam(
            "keyword",
            required = false
        ) @Parameter(description = "关键词") keyword: String?,
        @RequestParam("page") @Parameter(
            description = "页码, 从1开始",
            required = true
        ) @Valid @NotNull @Min(1) page: Int?,
        @RequestParam("limit") @Parameter(
            description = "每页数量, 大于0",
            required = true
        ) @Valid @NotNull @Min(1) limit: Int?
    ) = notificationService.list(keyword, page!!, limit!!)

    @ServiceLog(description = "是否有未读通知")
    @AuthCheck
    @GetMapping("/hasUnreadNotifications")
    @Tag(name = "通知服务接口")
    @Operation(
        summary = "是否有未读通知",
        description = "是否有未读通知"
    )
    fun hasUnreadNotifications() = notificationService.hasUnreadNotifications()
}