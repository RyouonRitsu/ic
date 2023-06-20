package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.domain.protocol.request.AddPaymentRequest
import com.ryouonritsu.ic.service.PaymentInfoService
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
@RequestMapping("/payment")
@Tag(name = "缴费信息接口")
class PaymentInfoController(
    private val paymentInfoService: PaymentInfoService
) {
    @ServiceLog(description = "添加新的缴费单")
    @PostMapping("/create")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "缴费信息接口")
    @Operation(summary = "增加新的缴费单", description = "需要填写3个参数")
    fun create(@RequestBody @Valid request: AddPaymentRequest) =
        paymentInfoService.create(request)

    @ServiceLog(description = "查询缴费信息列表")
    @GetMapping("/list")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN, AuthEnum.CLIENT])
    @Tag(name = "缴费信息接口")
    @Operation(summary = "查询缴费信息列表", description = "不填则不筛选该项")
    fun list(
        @RequestParam(
            "ids",
            required = false
        ) @Parameter(description = "租赁信息id集合，如1,2,3") ids: List<Long>?,
        @RequestParam(
            "userId",
            required = false
        ) @Parameter(description = "客户id") userId: Long?,
        @RequestParam(
            "rentalId",
            required = false
        ) @Parameter(description = "租赁信息id") rentalId: Long?,
        @RequestParam(
            "roomId",
            required = false
        ) @Parameter(description = "房间id") roomId: Long?,
        @RequestParam("page") @Parameter(
            description = "页码, 从1开始",
            required = true
        ) @Valid @NotNull @Min(1) page: Int?,
        @RequestParam("limit") @Parameter(
            description = "每页数量, 大于0",
            required = true
        ) @Valid @NotNull @Min(1) limit: Int?
    ) = paymentInfoService.list(ids, userId, rentalId, roomId, page!!, limit!!)

    @ServiceLog(description = "查询缴费状态")
    @GetMapping("/queryStatus")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN, AuthEnum.CLIENT])
    @Tag(name = "缴费信息接口")
    @Operation(summary = "查询缴费状态", description = "按年给出缴费状态")
    fun queryStatusByUserId(
        @RequestParam(
            "userId",
            required = false
        ) @Parameter(description = "客户id") userId: Long?,
    ) = paymentInfoService.queryStatusByUserId(userId)
}