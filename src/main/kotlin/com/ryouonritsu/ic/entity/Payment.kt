package com.ryouonritsu.ic.entity

import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

/**
 * @author PaulManstein
 */
@Entity
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '缴费单ID", nullable = false)
    var id: Long = 0,
    @Column(columnDefinition = "BIGINT COMMENT '租户ID", nullable = false)
    var userid: Long = 0,
    @Column(columnDefinition = "BIGINT COMMENT '租赁合同ID", nullable = false)
    var rentid: Long = 0,
    @Column(columnDefinition = "BIGINT COMMENT '房间ID", nullable = false)
    var roomid: Long = 0,
    @Column(columnDefinition = "DATE DEFAULT '1900-01-01' COMMENT '缴费时间'", nullable = false)
    var paytime: LocalDate = LocalDate.of(1900,1,1),
    @Column(columnDefinition = "BigDemical DEFAULT 0.0 COMMENt '缴纳金额", nullable = false)
    var expense: BigDecimal = BigDecimal(0.0)
)