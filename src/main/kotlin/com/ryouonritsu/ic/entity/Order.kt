package com.ryouonritsu.ic.entity

import com.ryouonritsu.ic.common.constants.ICConstant.EMPTY_STR
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
@Table(name = "`order`")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '订单ID'", nullable = false)
    var id: Long = 0,
    @Column(
        name = "user_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '用户ID'",
        nullable = false
    )
    var userId: Long,
    @Column(name = "goods_info", columnDefinition = "TEXT COMMENT '商品信息'")
    var goodsInfo: String,
    @Column(columnDefinition = "VARCHAR(511) DEFAULT '' COMMENT '收货地址'", nullable = false)
    var address: String = EMPTY_STR,
    @Column(columnDefinition = "DECIMAL(30, 6) DEFAULT '0' COMMENT '价格'", nullable = false)
    var price: BigDecimal,
    @Column(columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '状态'", nullable = false)
    var state: Int = 0,
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
    enum class State(
        val code: Int,
        val desc: String
    ) {
        UNPAID(0, "未支付"),
        PAID(1, "已支付"),
        REFUNDING(2, "退款中"),
        REFUNDED(3, "已退款"),
        CANCELLED(4, "已取消");

        companion object {
            fun valueOf(code: Int) = values().find { it.code == code } ?: UNPAID
            fun getByDesc(desc: String) = values().find { it.desc == desc } ?: UNPAID
        }

        operator fun invoke() = this.desc
    }
}