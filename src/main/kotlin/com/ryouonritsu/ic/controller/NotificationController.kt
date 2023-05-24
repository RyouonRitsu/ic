package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
import com.ryouonritsu.ic.domain.protocol.request.SubscribeRequest
import com.ryouonritsu.ic.domain.protocol.request.UnsubscribeRequest
import com.ryouonritsu.ic.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

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

    @ServiceLog(description = "订阅通知")
    @PostMapping("/subscribe")
    @Tag(name = "通知服务接口")
    @Operation(
        summary = "订阅通知",
        description = "订阅通知, 会将订阅者的邮箱加入到该通知的订阅者列表中, 当前如有pending状态的该通知, 会尝试立即发送通知到该订阅者"
    )
    fun subscribe(@Valid @RequestBody request: SubscribeRequest) =
        notificationService.subscribe(request)

    @ServiceLog(description = "取消订阅通知")
    @PostMapping("/unsubscribe")
    @Tag(name = "通知服务接口")
    @Operation(
        summary = "取消订阅通知",
        description = "取消订阅通知, 会将订阅者的邮箱从该通知的订阅者列表中移除"
    )
    fun unsubscribe(@Valid @RequestBody request: UnsubscribeRequest) =
        notificationService.unsubscribe(request)

    @ServiceLog(description = "发送验证码")
    @GetMapping("/sendVerificationCode")
    @Tag(name = "通知服务接口")
    @Operation(
        summary = "发送验证码",
        description = "发送验证码, 会向指定邮箱发送验证码, 该验证码用于取消订阅通知时的身份验证"
    )
    fun sendVerificationCode(@Valid @RequestParam @NotBlank @Email email: String = "") =
        notificationService.sendVerificationCode(email)
}