package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.domain.protocol.request.CreateVisitorRequest
import com.ryouonritsu.ic.service.VisitorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * @Author Kude
 * @Date 2023/6/26 10:43
 */
@Validated
@RestController
@RequestMapping("/visitor")
@Tag(name = "访客接口")
class VisitorController(
    private val visitorService: VisitorService,
) {
    @ServiceLog(description = "客户创建访客申请")
    @PostMapping("/createVisitor")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN, AuthEnum.CLIENT])
    @Tag(name = "访客接口")
    @Operation(
        summary = "客户创建访客申请",
        description = "由客户公司邀请人在系统提出访客申请，包括访客人员姓名、身份证号码、到访时间（具体到某日几点）、手机号码。"
    )
    fun createVisitor(@RequestBody @Valid request: CreateVisitorRequest) =
        visitorService.createVisitor(request)

    @ServiceLog(description = "访客月统计")
    @GetMapping("/statisticsMonth")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "访客接口")
    @Operation(
        summary = "管理员统计月访客",
        description = "按月返回"
    )
    fun statisticsMonth(
        @RequestParam(
            "year",
            required = true
        ) @Parameter(description = "统计年份")
        @Valid @NotNull year: Int,
    ) = visitorService.statisticsMonth(year)

    @ServiceLog(description = "访客日统计")
    @GetMapping("/statisticsDay")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "访客接口")
    @Operation(
        summary = "管理员统计日访客",
        description = "按日返回"
    )
    fun statisticsDay(
        @RequestParam(
            "date",
            required = true
        ) @Parameter(description = "统计日期，示例：2011-12-03")
        @Valid @NotNull @NotBlank date: String,
    ) = visitorService.statisticsDay(date)
}