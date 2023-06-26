package com.ryouonritsu.ic.entity

import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.toJSONString
import com.ryouonritsu.ic.domain.dto.VisitorDTO
import com.ryouonritsu.ic.domain.dto.VisitorInfoDTO
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @Author Kude
 * @Date 2023/6/26 10:02
 */
@Entity
class Visitor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '访客ID'", nullable = false)
    var id: Long = 0,
    @Column(
        name = "custom_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '邀请人ID'",
        nullable = false
    )
    var customId: Long,
    @Column(
        name = "visitor_name",
        columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '访客人员姓名'",
        nullable = false
    )
    var visitorName: String,
    @Column(
        name = "card_number",
        columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '身份证号码'",
        nullable = false
    )
    var cardNumber: String,
    @Column(
        name = "phone_number",
        columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '电话号码'",
        nullable = false
    )
    var phoneNumber: String,
    @Column(
        name = "visit_time",
        columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '到访时间'",
        nullable = false
    )
    var visitTime: LocalDateTime = LocalDateTime.now(),
    @Column(
        name = "visit_status",
        columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '访问状态'",
        nullable = false
    )
    var visitStatus: Int = 0,
    @Column(name = "visitor_info", columnDefinition = "TEXT COMMENT '访客其他信息JSON'")
    var visitorInfo: String = VisitorInfoDTO().toJSONString(),
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
    fun toDTO() = VisitorDTO(
        id = "$id",
        customId = "$customId",
        visitorName = visitorName,
        cardNumber = cardNumber,
        phoneNumber = phoneNumber,
        visitTime = visitTime,
        visitStatus = "$visitStatus",
        visitorInfo = visitorInfo.to(),
        createTime = createTime,
        modifyTime = modifyTime,
    )
}