package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.domain.dto.PaymentInfoDTO
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
class PaymentInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '缴费信息ID'", nullable = false)
    var id: Long = 0,
    @Column(name = "user_id", columnDefinition = "BIGINT COMMENT '客户ID'", nullable = false)
    var userId: Long,
    @Column(name = "rental_id", columnDefinition = "BIGINT COMMENT '租赁信息ID'", nullable = false)
    var rentalId: Long,
    @Column(name = "room_id", columnDefinition = "BIGINT COMMENT '房间ID'", nullable = false)
    var roomId: Long,
    @Column(columnDefinition = "DECIMAL(30, 6) DEFAULT '0' COMMENT '缴纳金额'", nullable = false)
    var amount: BigDecimal,
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
    fun toDTO() = PaymentInfoDTO(
        id = "$id",
        userId = "$userId",
        rentalId = "$rentalId",
        roomId = "$roomId",
        paymentTime = createTime,
        amount = amount.toString()
    )
}