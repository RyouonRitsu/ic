package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.service.MROService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * @Author Kude
 * @Date 2023/6/16 14:41
 */
@Validated
@RestController
@RequestMapping("/mro")
@Tag(name = "维修工单接口")
class MROController(
        private val redisUtils: RedisUtils,
        private val mroService: MROService,
) {
    companion object {
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }

    @ServiceLog(description = "根据客户id查询所有维修工单")
    @GetMapping("/selectMROByCustomId")
    @Tag(name = "维修工单接口")
    @Operation(summary = "根据用户id查询所有维修工单")
    fun selectUserByCustomId(
            @RequestParam("custom_id") @Parameter(
                    description = "客户id",
                    required = true
            ) userId: Long
    ) = mroService.selectMROByCustomId(userId)

    @ServiceLog(description = "根据维修工作人员id查询所有维修工单")
    @GetMapping("/selectMROByWorkerId")
    @Tag(name = "维修工单接口")
    @Operation(summary = "根据维修工作人员id查询所有维修工单")
    fun selectUserByWorkerId(
        @RequestParam("worker_id") @Parameter(
            description = "维修工作人员id",
            required = true
        ) userId: Long
    ) = mroService.selectMROByWorkerId(userId)

}