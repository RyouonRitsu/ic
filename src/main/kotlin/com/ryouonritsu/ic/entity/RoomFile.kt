package com.ryouonritsu.ic.entity

import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author PaulManstein
 */
@Entity
class RoomFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '文件ID'", nullable = false)
    var id: Long = 0,
    @Column(columnDefinition = "TEXT COMMENT '文件URL'")
    var url: String,
    @Column(name = "file_path", columnDefinition = "TEXT COMMENT '文件路径'")
    var filePath: String = "",
    @Column(name = "file_name", columnDefinition = "TEXT COMMENT '文件名'")
    var fileName: String = "",
    @Column(
        name = "room_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '房间ID'",
        nullable = false
    )
    var roomId: Long,
    @Column(
        name = "is_deleted",
        columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '是否已删除'",
        nullable = false
    )
    var isDeleted: Boolean = false,
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
}