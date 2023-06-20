package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.common.utils.DownloadUtils
import com.ryouonritsu.ic.service.RoomService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * @author PaulManstein
 */
@Validated
@RestController
@RequestMapping("/room")
@Tag(name = "房间接口")
class RoomController(
    private val roomService: RoomService
) {
    companion object {
        private val log = LoggerFactory.getLogger(RoomController::class.java)
    }

    @ServiceLog(description = "根据房间ID查询房间信息")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @GetMapping("/selectRoomByRoomId")
    @Tag(name = "房间接口")
    @Operation(summary = "根据房间ID查询房间信息")
    fun selectRoomByRoomId(
        @RequestParam("room_id") @Parameter(
            description = "房间id",
            required = true
        ) roomId: Long
    ) = roomService.selectRoomById(roomId)

    @ServiceLog(description = "查询房间列表")
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @GetMapping("/list")
    @Tag(name = "房间接口")
    @Operation(
        summary = "将全部房间列出",
        description = "按照房间id升序"
    )
    fun list(
        @RequestParam(
            "id",
            required = false
        ) @Parameter(description = "id,精确") id: Long?,
        @RequestParam(
            "userId",
            required = false
        ) @Parameter(description = "租户id,模糊") userId: Long?,
        @RequestParam(
            "status",
            required = false
        ) @Parameter(description = "租赁状态,精确") status: Boolean?,
        @RequestParam(
            "commence",
            required = false
        ) @Parameter(description = "租赁开始日期,精确") commence: LocalDate?,
        @RequestParam(
            "terminate",
            required = false
        ) @Parameter(description = "租赁结束日期,精确") terminate: LocalDate?,
        @RequestParam(
            "contract",
            required = false
        ) @Parameter(description = "合同Id,精确") contract: Long?,
        @RequestParam(
            "roomInfo",
            required = false
        ) @Parameter(description = "房间其他信息,模糊") roomInfo: String?,
        @RequestParam("page") @Parameter(
            description = "页码, 从1开始",
            required = true
        ) @Valid @NotNull @Min(1) page: Int?,
        @RequestParam("limit") @Parameter(
            description = "每页数量, 大于0",
            required = true
        ) @Valid @NotNull @Min(1) limit: Int?
    ) = roomService.list(
        id, userId, status, commence, terminate, contract, roomInfo, page!!, limit!!
    )

    @ServiceLog(description = "查询房间列表表头")
    @AuthCheck
    @GetMapping("/queryHeaders")
    @Tag(name = "房间接口")
    @Operation(
        summary = "查询房间列表表头",
        description = "查询房间列表表头"
    )
    fun queryHeaders() = roomService.queryHeaders()

    @ServiceLog(description = "房间上传模板下载", printResponse = false)
    @AuthCheck(auth = [AuthEnum.TOKEN, AuthEnum.ADMIN])
    @GetMapping("/downloadTemplate")
    @Tag(name = "房间接口")
    @Operation(
        summary = "房间上传模板下载",
        description = "房间上传模板下载"
    )
    fun downloadTemplate(): ResponseEntity<ByteArray> {
        try {
            roomService.downloadTemplate().use { wb ->
                ByteArrayOutputStream().use { os ->
                    wb.write(os)
                    return DownloadUtils.downloadFile("room_template.xlsx", os.toByteArray())
                }
            }
        } catch (e: Exception) {
            log.error("[RoomController.downloadTemplate] failed to download rooms template")
            throw e
        }
    }
}