package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.domain.dto.RentalInfoDTO
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class RentalInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '租赁信息ID'", nullable = false)
    var id: Long = 0,
    @Column(
        name = "user_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '客户ID'",
        nullable = false
    )
    var userId: Long,
    @Column(
        name = "room_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '房间ID'",
        nullable = false
    )
    var roomId: Long,
    @Column(
        name = "start_time",
        columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间'",
        nullable = false
    )
    var startTime: LocalDate,
    @Column(
        name = "end_time",
        columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间'",
        nullable = false
    )
    var endTime: LocalDate,
    @Column(
        name = "total_cost",
        columnDefinition = "DECIMAL(30, 6) DEFAULT '0' COMMENT '租赁总费用'",
        nullable = false
    )
    var totalCost: BigDecimal,
    @Column(columnDefinition = "TINYINT(3) DEFAULT '1' COMMENT '生效状态'", nullable = false)
    var status: Boolean = true,
    @Column(columnDefinition = "INT DEFAULT '1' COMMENT '版本'", nullable = false)
    var version: Int = 1,
    @Column(
        name = "create_time",
        columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'",
        nullable = false
    )
    var createTime: LocalDateTime = LocalDateTime.now(),
    @Column(
        name = "modify_time",
        columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'",
        nullable = false
    )
    var modifyTime: LocalDateTime = LocalDateTime.now(),
) {
    fun toDTO() = RentalInfoDTO(
        id = "$id",
        userId = "$userId",
        roomId = "$roomId",
        startTime = startTime,
        endTime = endTime,
        totalCost = totalCost.toString(),
        signTime = createTime
    )
}