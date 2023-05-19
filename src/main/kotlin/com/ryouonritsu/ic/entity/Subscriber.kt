package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.domain.dto.SubscriberDTO
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
class Subscriber(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '订阅者ID'", nullable = false)
    var id: Long = 0,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '订阅标签'", nullable = false)
    var tag: String,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '订阅邮箱'", nullable = false)
    var email: String,
    @Column(
        columnDefinition = "TINYINT(3) DEFAULT '1' COMMENT '生效状态, 1生效, 0未生效'",
        nullable = false
    )
    var status: Boolean = true,
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
    fun toDTO() = SubscriberDTO(
        id = "$id",
        tag = tag,
        email = email,
    )
}