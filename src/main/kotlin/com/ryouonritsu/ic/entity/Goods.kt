package com.ryouonritsu.ic.entity

import com.alibaba.fastjson2.JSONObject
import com.ryouonritsu.ic.common.constants.ICConstant.EMPTY_STR
import com.ryouonritsu.ic.domain.dto.GoodsDTO
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
class Goods(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '商品ID'", nullable = false)
    var id: Long = 0,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '商品名称'", nullable = false)
    var name: String,
    @Column(columnDefinition = "TEXT COMMENT '描述图片地址'")
    var picture: String = EMPTY_STR,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '商品类型'", nullable = false)
    var type: String = EMPTY_STR,
    @Column(columnDefinition = "BIGINT DEFAULT '0' COMMENT '数量'", nullable = false)
    var amount: Long = 0,
    @Column(columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '状态'", nullable = false)
    var state: Int = 0,
    @Column(columnDefinition = "DECIMAL(30, 6) DEFAULT '0' COMMENT '价格'", nullable = false)
    var price: BigDecimal = BigDecimal.ZERO,
    @Column(columnDefinition = "DECIMAL(10, 6) DEFAULT '0' COMMENT '折扣'", nullable = false)
    var discount: BigDecimal = BigDecimal.ONE,
    @Column(columnDefinition = "TEXT COMMENT '商品描述'")
    var description: String = EMPTY_STR,
    @Column(
        name = "view_cnt",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '查询计数'",
        nullable = false
    )
    var viewCnt: Long = 0,
    @Column(columnDefinition = "BIGINT DEFAULT '0' COMMENT '销量'", nullable = false)
    var sales: Long = 0,
    @Column(name = "goods_info", columnDefinition = "LONGTEXT COMMENT '商品信息JSON'")
    var goodsInfo: String = JSONObject().toJSONString(),
    @Column(
        name = "user_id",
        columnDefinition = "BIGINT DEFAULT '0' COMMENT '用户ID'",
        nullable = false
    )
    var userId: Long = 0,
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
        UNDER_REVIEW(0, "审核中"),
        ON_SALE(1, "在售"),
        DISCOUNT(2, "折扣"),
        SOLD_OUT(3, "售磬"),
        EXPIRE(4, "过期");

        companion object {
            fun valueOf(code: Int) = values().find { it.code == code } ?: UNDER_REVIEW
            fun getByDesc(desc: String) = values().find { it.desc == desc } ?: UNDER_REVIEW
        }

        operator fun invoke() = this.desc
    }

    fun toDTO() = GoodsDTO(
        id = "$id",
        name, picture, type,
        amount = "$amount",
        state = State.valueOf(state)(),
        originalPrice = price.toString(),
        discount = discount.toString(),
        price = (price * discount).toString(),
        description,
        viewCnt = "$viewCnt",
        sales = "$sales",
        createTime, modifyTime
    )
}