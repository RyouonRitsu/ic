package com.ryouonritsu.ic.controller


import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.domain.protocol.request.CreateRentalInfoRequest
import com.ryouonritsu.ic.service.RentalInfoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.jetbrains.annotations.NotNull
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Min

@Validated
@RestController
@RequestMapping("/rentalInfo")
@Tag(name="租赁信息接口")
class RentalInfoController(
    private val redisUtils: RedisUtils,
    private val rentalInfoService: RentalInfoService,
) {
    companion object {
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }

    @ServiceLog(description = "查询租赁信息列表")
    @GetMapping("/list")
    @AuthCheck(auth = [AuthEnum.TOKEN])
    @Tag(name = "租赁信息接口")
    @Operation(summary = "查询租赁信息列表")
    fun list(
        @RequestParam(
            "id",
            required = false
        )@Parameter(description = "租赁信息id，精确") id: String?,

        @RequestParam(
            required = false
        )@Parameter(description = "客户id，精确") customId: String?,

        @RequestParam(
            required = false
        )@Parameter(description = "房间id，精确") roomId: String?,

        @RequestParam("page")@Parameter(
            description = "页码, 从1开始",
            required = true
        ) @Valid @NotNull @Min(1) page: Int?,

        @RequestParam("limit") @Parameter(
            description = "每页数量, >0",
            required = true
        ) @Valid @NotNull @Min(1) limit: Int?,

        ) = rentalInfoService.list(
        id,
        customId,
        roomId,
        page!!,
        limit!!
    )


    @ServiceLog(description = "创建租赁信息")
    @PostMapping("/createRentalInfo")
    @AuthCheck(auth = [AuthEnum.TOKEN])
    @Tag(name = "租赁信息接口")
    @Operation(
        summary = "创建租赁信息",
        description = "创建租赁信息"
    )
    fun createRentalInfo(
        @RequestBody @Valid request : CreateRentalInfoRequest)
    = rentalInfoService.createRentalInfo(request)


}