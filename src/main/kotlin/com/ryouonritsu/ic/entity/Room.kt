package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.domain.dto.RoomDTO
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author PaulManstein
 */
@Entity
class Room(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '房间ID'", nullable = false)
    var id: Long = 0,
    @Column(name = "user_id", columnDefinition = "BIGINT COMMENT '用户ID'", nullable = true)
    var userId: Long?,
    @Column(columnDefinition = "BIGINT COMMENT '租赁状态'", nullable = false)
    var status: Boolean = false,
    @Column(columnDefinition = "DATE COMMENT '签约时间'", nullable = true)
    var commence: LocalDate?,
    @Column(columnDefinition = "DATE COMMENT '租赁结束时间'", nullable = true)
    var terminate: LocalDate?,
    @Column(columnDefinition = "BIGINT COMMENT '合同ID'", nullable = true)
    var contract: Long?,
    @Column(name = "room_info", columnDefinition = "LONGTEXT COMMENT '房间信息JSON'")
    var roomInfo: String = "",
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
    fun toDTO() = RoomDTO(
        id = "$id",
        userId = "$userId",
        status = status,
        commence, terminate,
        contract = "$contract",
        roomInfo
    )
}