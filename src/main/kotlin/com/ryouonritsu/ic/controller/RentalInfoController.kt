package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.domain.protocol.request.CreateRentalInfoRequest
import com.ryouonritsu.ic.service.RentalInfoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@Validated
@RestController
@RequestMapping("/rentalInfo")
@Tag(name = "租赁信息接口")
class RentalInfoController(
    private val rentalInfoService: RentalInfoService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @ServiceLog(description = "查询租赁信息列表")
    @GetMapping("/list")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN, AuthEnum.CLIENT])
    @Tag(name = "租赁信息接口")
    @Operation(summary = "查询租赁信息列表", description = "不填则不筛选该项")
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
            "roomId",
            required = false
        ) @Parameter(description = "房间id") roomId: Long?,
        @RequestParam(
            "startTime",
            required = false
        ) @Parameter(description = "开始时间，如2019/03/14") startTime: LocalDate?,
        @RequestParam(
            "endTime",
            required = false
        ) @Parameter(description = "结束时间，如2024/09/03") endTime: LocalDate?,
        @RequestParam("page") @Parameter(
            description = "页码, 从1开始",
            required = true
        ) @Valid @NotNull @Min(1) page: Int?,
        @RequestParam("limit") @Parameter(
            description = "每页数量, 大于0",
            required = true
        ) @Valid @NotNull @Min(1) limit: Int?
    ) = rentalInfoService.list(ids, userId, roomId, startTime, endTime, page!!, limit!!)

    @ServiceLog(description = "创建租赁信息")
    @PostMapping("/createRentalInfo")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "租赁信息接口")
    @Operation(summary = "创建租赁信息", description = "创建租赁信息")
    fun createRentalInfo(@RequestBody @Valid request: CreateRentalInfoRequest) =
        rentalInfoService.createRentalInfo(request)
}