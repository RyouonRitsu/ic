package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.domain.dto.EventDTO
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '事件ID'", nullable = false)
    var id: Long = 0,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '订阅标签'", nullable = false)
    var tag: String,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '事件名'", nullable = false)
    var name: String,
    @Column(columnDefinition = "MEDIUMTEXT COMMENT '事件内容'")
    var message: String,
    @Column(
        columnDefinition = "TINYINT(3) DEFAULT '1' COMMENT '事件状态, 1未分发, 0已分发'",
        nullable = false
    )
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
    fun toDTO() = EventDTO(
        id = "$id",
        tag = tag,
        name = name,
        message = message,
        createTime = createTime,
        modifyTime = modifyTime,
    )
}