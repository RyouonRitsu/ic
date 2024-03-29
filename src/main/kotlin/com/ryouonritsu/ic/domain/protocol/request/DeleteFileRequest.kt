package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

/**
 * @author ryouonritsu
 */
@Schema(description = "删除文件请求")
data class DeleteFileRequest(
    @field:NotBlank
    @Schema(description = "文件分享链接", required = true)
    val url: String?
)
