package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.domain.protocol.request.CreateVisitorRequest
import com.ryouonritsu.ic.service.VisitorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

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
}