package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.utils.DownloadUtils
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.domain.protocol.request.RoomUploadRequest
import com.ryouonritsu.ic.service.RoomService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayOutputStream
import javax.validation.Valid

/**
 * @author PaulManstein
 */
@Validated
@RestController
@RequestMapping("/room")
@Tag(name = "房间接口")
class RoomController(
    private val roomService: RoomService,
    private val redisUtils: RedisUtils
) {
    companion object{
        private val log = LoggerFactory.getLogger(RoomController::class.java)
    }

    @ServiceLog(description = "根据房间ID查询房间信息")
    @GetMapping("/selectRoomByRoomId")
    @Tag(name = "房间接口")
    @Operation(summary = "根据房间ID查询房间信息")
    fun selectRoomByRoomId(
        @RequestParam("room_id") @Parameter(
            description = "房间id",
            required = true
        ) roomId: Long
    ) = roomService.selectRoomById(roomId)//

    @ServiceLog(description = "上传文件", printRequest = false)
    @PostMapping("/uploadFile")
    @Tag(name = "房间接口")
    @Operation(
        summary = "上传文件",
        description = "将用户上传的文件保存在静态文件目录static/file/\${room_id}/\${file_name}下"
    )
    fun uploadFile(@ModelAttribute @Valid request: RoomUploadRequest) =
        roomService.uploadFile(request.file!!)

    @ServiceLog(description = "查询房间列表表头")
    @GetMapping("/queryHeaders")
    @Tag(name = "房间接口")
    @Operation(
        summary = "查询房间列表表头",
        description = "查询房间列表表头"
    )
    fun queryHeaders() = roomService.queryHeaders()

    @ServiceLog(description = "房间上传模板下载", printResponse = false)
    @GetMapping("/downloadTemplate")
    @Tag(name = "房间接口")
    @Operation(
        summary = "房间上传模板下载",
        description = "房间上传模板下载"
    )
    fun downloadTemplate(): ResponseEntity<ByteArray>{
        try{
            roomService.downloadTemplate().use{ wb ->
                ByteArrayOutputStream().use { os ->
                    wb.write(os)
                    return DownloadUtils.downloadFile("room_template.xlsx", os.toByteArray())
                }
            }
        } catch (e:Exception){
            log.error("[RoomController.downloadTemplate] failed to download rooms template")
            throw e
        }
    }
}