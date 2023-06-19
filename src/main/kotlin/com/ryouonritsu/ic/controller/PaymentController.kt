package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.domain.protocol.request.AddPaymentRequest
import com.ryouonritsu.ic.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

/**
 * @author PaulManstein
 */

@Validated
@RestController
@RequestMapping("/payment")
@Tag(name = "缴费单接口")
class PaymentController(
    private val paymentService: PaymentService,
    private val redisUtils: RedisUtils
) {
    @ServiceLog(description = "添加新的缴费单")
    @PostMapping("/addPayment")
    @Tag(name = "缴费单接口")
    @Operation(summary = "增加新的缴费单", description = "需要填写5个参数")
    fun addPayment(@RequestBody @Valid request: AddPaymentRequest) = paymentService.addPayment(request)
}