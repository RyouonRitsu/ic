package com.ryouonritsu.ic.domain.protocol.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull
import org.springframework.web.multipart.MultipartFile

/**
 * @author PaulManstein
 */

@Schema(description = "房间上传请求")
data class RoomUploadRequest(
    @field:JsonIgnore
    @field:NotNull
    @Schema(description = "Excel文件", required = true)
    val file: MultipartFile?
)