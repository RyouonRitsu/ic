package com.ryouonritsu.ic.entity

import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
class CartRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '购物车记录ID'", nullable = false)
    var id: Long = 0,
    @Column(
        name = "user_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '用户ID'",
        nullable = false
    )
    var userId: Long,
    @Column(
        name = "goods_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '商品ID'",
        nullable = false
    )
    var goodsId: Long,
    @Column(columnDefinition = "BIGINT DEFAULT '0' COMMENT '数量'", nullable = false)
    var amount: Long,
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
}