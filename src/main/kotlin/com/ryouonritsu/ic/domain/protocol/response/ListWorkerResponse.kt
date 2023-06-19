package com.ryouonritsu.ic.domain.protocol.response

import com.ryouonritsu.ic.domain.dto.UserDTO
import io.swagger.v3.oas.annotations.media.Schema

/**
 * @Author Kude
 * @Date 2023/6/19 11:41
 */
@Schema(description = "维修工人查询响应")
data class ListWorkerResponse(
    @Schema(description = "总数", required = true)
    val total: Int,
    @Schema(description = "列表", required = true)
    val list: List<UserDTO>,
)