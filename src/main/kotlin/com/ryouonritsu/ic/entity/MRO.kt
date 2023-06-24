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
        name = "custom_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '客户ID'",
        nullable = false
    )
    var customId: Long,
    @Column(
        name = "worker_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '维修工作人员ID'",
        nullable = false
    )
    var workerId: Long = 0,
    @Column(
        name = "room_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '房间ID'",
        nullable = false
    )
    var roomId: Long,
    @Column(columnDefinition = "LONGTEXT COMMENT '问题描述'", nullable = false)
    var problem: String = "",
    @Column(name = "expect_time", columnDefinition = "TEXT COMMENT '期望时间'", nullable = false)
    var expectTime: String = "",
    @Column(name = "actual_date", columnDefinition = "TEXT COMMENT '实际日期'", nullable = false)
    var actualDate: String = "",
    @Column(name = "actual_time", columnDefinition = "TEXT COMMENT '实际时间段'", nullable = false)
    var actualTime: String = "",
    @Column(columnDefinition = "LONGTEXT COMMENT '问题解决方法'", nullable = false)
    var resolvent: String = "",
    @Column(
        name = "maintenance_time",
        columnDefinition = "TEXT COMMENT '具体维修时间'",
        nullable = false
    )
    var maintenanceTime: String = "",
    @Column(
        name = "mro_status",
        columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '订单状态'",
        nullable = false
    )
    var mroStatus: Int = 0,
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
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '维修类型标签'", nullable = false)
    var label: String = "",
) {
    fun toDTO() = MRODTO(
        id = "$id",
        customId = "$customId",
        userInfo = null,
        workerInfo = null,
        workerId = "$workerId",
        roomId = "$roomId",
        problem = problem,
        expectTime = expectTime,
        actualDate = actualDate,
        actualTime = actualTime,
        resolvent = resolvent,
        maintenanceTime = maintenanceTime,
        mroStatus = "$mroStatus",
        createTime = createTime,
        modifyTime = modifyTime,
        label = label,
    )
}