package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.domain.protocol.request.AdminModifyMRORequest
import com.ryouonritsu.ic.domain.protocol.request.CreateMRORequest
import com.ryouonritsu.ic.domain.protocol.request.WorkerModifyMRORequest
import com.ryouonritsu.ic.service.MROService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
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
    private val mroService: MROService,
) {
    companion object {
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }

    @ServiceLog(description = "查询维修工单列表")
    @GetMapping("/list")
    @AuthCheck(auth = [AuthEnum.TOKEN])
    @Tag(name = "维修工单接口")
    @Operation(summary = "查询维修工单列表")
    fun list(
        @RequestParam(
            "id",
            required = false
        ) @Parameter(description = "维修工单id，精确") id: String?,
        @RequestParam(
            required = false
        ) @Parameter(description = "客户id，精确") customId: String?,
        @RequestParam(
            required = false
        ) @Parameter(description = "维修人员id，精确") workerId: String?,
        @RequestParam(
            required = false
        ) @Parameter(description = "房间id，精确") roomId: String?,
        @RequestParam(
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

    @ServiceLog(description = "用户创建维修工单")
    @PostMapping("/createMRO")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN, AuthEnum.CLIENT])
    @Tag(name = "维修工单接口")
    @Operation(
        summary = "用户创建维修工单",
        description = "由客户创建维修工单，填写问题描述、期望时间段（多个时间段、分上下午）、报修房间号"
    )
    fun createMRO(@RequestBody @Valid request: CreateMRORequest) =
        mroService.createMRO(request)

    @ServiceLog(description = "管理员修改维修工单")
    @PostMapping("/adminModifyMRO")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "维修工单接口")
    @Operation(
        summary = "管理员修改维修工单",
        description = "管理员查看问题描述后根据维修类型和期望时间段筛选可用维修人员，填入维修人员id和实际时间段"
    )
    fun adminModifyMRO(@RequestBody @Valid request: AdminModifyMRORequest) =
        mroService.adminModifyMRO(request)

    @ServiceLog(description = "维修人员修改维修工单")
    @PostMapping("/workerModifyMRO")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN, AuthEnum.MAINTENANCE_STAFF])
    @Tag(name = "维修工单接口")
    @Operation(
        summary = "维修人员修改维修工单",
        description = "维修人员维修完成后，填写解决办法，解决时间，并自动更新为已解决"
    )
    fun workerModifyMRO(@RequestBody @Valid request: WorkerModifyMRORequest) =
        mroService.workerModifyMRO(request)

    @ServiceLog(description = "维修人员筛选")
    @GetMapping("/selectWorker")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @Tag(name = "维修工单接口")
    @Operation(
        summary = "维修人员筛选",
        description = "根据时间和维修类型筛选可用维修人员"
    )
    fun selectWorker(
        @RequestParam(required = false)
        @Parameter(description = "具体维修日期") actualDate: String?,
        @RequestParam(required = false)
        @Parameter(description = "具体维修时间段") actualTime: String?,
        @RequestParam(required = false)
        @Parameter(description = "具体维修类型") label: String?
    ) = mroService.selectWorker(actualDate, actualTime, label)
}