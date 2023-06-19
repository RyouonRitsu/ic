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
        name = "custom_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '客户ID'",
        nullable = false
    )
    var customId: Long,

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
        name = "sign_time",
        columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签约时间'",
        nullable = false
    )
    var signTime: LocalDateTime = LocalDateTime.now(),

    @Column(
        name="total_cost",
        columnDefinition = "INT DEFAULT '0' COMMENT '租赁总费用'",
        nullable = false
    )
    var totalCost: BigDecimal,

){
    fun toDTO() = RentalInfoDTO(
        id = "$id",
        customId = "$customId",
        roomId = "$roomId",
        startTime = startTime,
        endTime = endTime,
        totalCost = "$totalCost",
    )
}