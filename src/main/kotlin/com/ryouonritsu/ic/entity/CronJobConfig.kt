package com.ryouonritsu.ic.entity

import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
class CronJobConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '定时任务ID'", nullable = false)
    var id: Long = 0,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '定时任务标识符'", nullable = false)
    var name: String,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT 'Cron表达式'", nullable = false)
    var expression: String,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '任务描述'", nullable = false)
    var description: String,
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
)