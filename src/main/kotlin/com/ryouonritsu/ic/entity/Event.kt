package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.common.constants.ICConstant
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.domain.dto.EventDTO
import com.ryouonritsu.ic.domain.protocol.request.PublishRequest
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
    @Column(name = "user_id", columnDefinition = "BIGINT COMMENT '用户ID'", nullable = false)
    var userId: Long,
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
    companion object {
        fun from(request: PublishRequest) = Event(
            userId = request.userId ?: throw ServiceException(ExceptionEnum.BAD_REQUEST),
            name = request.name ?: ICConstant.EVENT,
            message = request.message ?: throw ServiceException(ExceptionEnum.BAD_REQUEST)
        )
    }

    fun toDTO() = EventDTO(
        id = "$id",
        userId = "$userId",
        name, message, status, createTime, modifyTime
    )
}