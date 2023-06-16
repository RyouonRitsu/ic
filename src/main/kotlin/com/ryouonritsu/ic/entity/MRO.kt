package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.domain.dto.MRODTO
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @Author Kude
 * @Date 2023/6/16 10:59
 */
@Entity
class MRO(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(columnDefinition = "BIGINT COMMENT '维修工单ID'", nullable = false)
        var id: Long = 0,
        @Column(
                name = "user_id",
                columnDefinition = "BIGINT DEFAULT '0' COMMENT '用户ID'",
                nullable = false
        )
        var userId: Long,
        @Column(
                name = "room_id",
                columnDefinition = "BIGINT DEFAULT '0' COMMENT '房间ID'",
                nullable = false
        )
        var roomId: Long,
        @Column(columnDefinition = "LONGTEXT COMMENT '问题描述'", nullable = false)
        var problem: String = "",
        @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '期望时间段'", nullable = false)
        var expectTime: String = "",
        @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '实际时间段'", nullable = false)
        var actualTime: String = "",
        @Column(columnDefinition = "LONGTEXT COMMENT '问题解决方法'", nullable = false)
        var resolvent: String = "",
        @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '维修时间'", nullable = false)
        var maintenanceTime: String = "",
        @Column(
                name = "is_solved",
                columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '是否解决'",
                nullable = false
        )
        var isSolved: Boolean = false,
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
    fun toDTO() = MRODTO(
            id = "$id",
            userId = "$userId",
            roomId = "$roomId",
            problem = problem,
            expectTime = expectTime,
            actualTime = actualTime,
            resolvent = resolvent,
            maintenanceTime = maintenanceTime,
            isSolved = isSolved,
    )
}