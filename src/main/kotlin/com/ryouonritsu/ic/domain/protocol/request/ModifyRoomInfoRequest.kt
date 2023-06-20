package com.ryouonritsu.ic.domain.protocol.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

/**
 * @author PaulManstein
 */

@Schema(description = "修改用户信息请求")
data class ModifyRoomInfoRequest(
    @field:NotNull
    @Schema(description = "房间Id， 管理员可用")
    var id: Long?,
    @Schema(description = "租户Id，管理员可用")
    var userId: Long?,
    @Schema(description = "租赁状态--是否已租出？， 管理员可用")
    var status: Boolean?,
    @Schema(description = "租赁开始时间")
    var commence: LocalDate?,
    @Schema(description = "租赁结束时间")
    var terminate: LocalDate?,
    @Schema(description = "合同Id")
    var contract: Long?,
    @Schema(description = "其他补充信息")
    var roomInfo: String?
)