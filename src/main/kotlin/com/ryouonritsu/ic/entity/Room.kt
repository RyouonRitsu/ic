package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.domain.dto.RoomDTO
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * @author PaulManstein
 */
@Entity
class Room(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '房间ID'", nullable = false)
    var id: Long = 0,
    @Column(columnDefinition = "BIGINT COMMENT '用户ID'", nullable = true)
    var userid: Long =0,
    @Column(columnDefinition = "BIGINT COMMENT '租赁状态'", nullable = false)
    var status: Long=0,
    @Column(columnDefinition = "DATE DEFAULT '1900-01-01' COMMENT '签约时间'", nullable = true)
    var commence: LocalDate = LocalDate.of(1900, 1, 1),
    @Column(columnDefinition = "DATE DEFAULT '1900-01-01' COMMENT '租赁结束时间'", nullable = true)
    var terminate: LocalDate= LocalDate.of(1900, 1,1),
    @Column(columnDefinition = "BIGINT COMMENT '合同ID'", nullable = true)
    var contract: LocalDate = LocalDate.of(1900,1, 1),
    @Column(name = "room_info", columnDefinition = "LONGTEXT COMMENT '用户信息JSON'")
    var roomInfo: String=""
){
    fun toDTO(): RoomDTO {
        TODO()
    }
}