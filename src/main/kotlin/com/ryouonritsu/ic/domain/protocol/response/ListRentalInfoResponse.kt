package com.ryouonritsu.ic.domain.protocol.response

import com.ryouonritsu.ic.domain.dto.RentalInfoDTO
import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "租赁信息查询响应")
data class ListRentalInfoResponse(
    @Schema(description = "总数", required = true)
    val total: Long,
    @Schema(description = "列表", required = true)
    val list: List<RentalInfoDTO>,
)