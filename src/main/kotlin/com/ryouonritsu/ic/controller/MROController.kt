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
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

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

    @ServiceLog(description = "查询维修工单列表")
    @GetMapping("/list")
    @Tag(name = "维修工单接口")
    @Operation(summary = "查询维修工单列表")
    fun list(
        @RequestParam(
            "id",
            required = false
        ) @Parameter(description = "维修工单id，精确") id: String?,
        @RequestParam(
            "custom_id",
            required = false
        ) @Parameter(description = "客户id，精确") customId: String?,
        @RequestParam(
            "worker_id",
            required = false
        ) @Parameter(description = "维修人员id，精确") workerId: String?,
        @RequestParam(
            "room_id",
            required = false
        ) @Parameter(description = "房间id，精确") roomId: String?,
        @RequestParam(
            "is_solved",
            required = false
        ) @Parameter(description = "是否解决，精确") isSolved: Boolean?,
        @RequestParam("page") @Parameter(
            description = "页码, 从1开始",
            required = true
        ) @Valid @NotNull @Min(1) page: Int?,
        @RequestParam("limit") @Parameter(
            description = "每页数量, 大于0",
            required = true
        ) @Valid @NotNull @Min(1) limit: Int?
    ) = mroService.list(
        id,
        customId,
        workerId,
        roomId,
        isSolved,
        page!!,
        limit!!
    )
}