package com.ryouonritsu.ic.entity

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.parseArray
import com.ryouonritsu.ic.domain.dto.UserDTO
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '用户ID'", nullable = false)
    var id: Long = 0,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '邮箱'", nullable = false)
    var email: String,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '用户名'", nullable = false)
    var username: String,
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '密码'", nullable = false)
    var password: String,
    @Column(columnDefinition = "TEXT COMMENT '头像地址'")
    var avatar: String = "",
    @Column(
        name = "legal_name",
        columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '法人名(真实姓名)'",
        nullable = false
    )
    var legalName: String = "",
    @Column(
        columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '性别, 0保密, 1男, 2女'",
        nullable = false
    )
    var gender: Int = 0,
    @Column(columnDefinition = "DATE DEFAULT '1900-01-01' COMMENT '生日'", nullable = false)
    var birthday: LocalDate = LocalDate.of(1900, 1, 1),
    @Column(
        name = "contact_name",
        columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '联系人名'",
        nullable = false
    )
    var contactName: String = "",
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '联系方式'", nullable = false)
    var phone: String = "",
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '所在地'", nullable = false)
    var location: String = "",
    @Column(
        name = "company_name",
        columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '公司名'",
        nullable = false
    )
    var companyName: String = "",
    @Column(name = "rental_info_ids", columnDefinition = "TEXT COMMENT '租赁信息id序列'")
    var rentalInfoIds: String = JSONArray().toJSONString(),
    @Column(name = "payment_info", columnDefinition = "TEXT COMMENT '缴纳信息id序列'")
    var paymentInfo: String = JSONArray().toJSONString(),
    @Column(columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '职位'", nullable = false)
    var position: String = "",
    @Column(
        name = "user_type",
        columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '用户类型'",
        nullable = false
    )
    var userType: Int = 0,
    @Column(name = "user_info", columnDefinition = "LONGTEXT COMMENT '用户信息JSON'")
    var userInfo: String = JSONObject().toJSONString(),
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
    enum class Gender(
        val code: Int,
        val desc: String
    ) {
        SECRET(0, "保密"),
        MALE(1, "男"),
        FEMALE(2, "女");

        companion object {
            fun valueOf(code: Int) = values().find { it.code == code } ?: SECRET
            fun getByDesc(desc: String) = values().find { it.desc == desc } ?: SECRET
        }
    }

    enum class UserType(
        val code: Int,
        val desc: String
    ) {
        CLIENT(0, "客户"),
        ADMIN(1, "管理员"),
        WATER_MAINTENANCE_STAFF(2, "水维修人员"),
        ELECTRICITY_MAINTENANCE_STAFF(3, "电维修人员"),
        MACHINE_MAINTENANCE_STAFF(4, "机器维修人员");

        companion object {
            fun valueOf(code: Int) = values().find { it.code == code } ?: CLIENT
            fun getByDesc(desc: String) = values().find { it.desc == desc } ?: CLIENT
        }

        operator fun invoke() = code
    }

    fun Int.toGender() = Gender.valueOf(this)
    fun Int.toUserType() = UserType.valueOf(this)
    fun toDTO() = UserDTO(
        id = "$id",
        email, username, avatar, legalName,
        gender = gender.toGender().desc,
        birthday, contactName, phone, location, companyName,
        rentalInfoIds = rentalInfoIds.parseArray<String>(),
        paymentInfo = paymentInfo.parseArray<String>(),
        position,
        userType = userType.toUserType().desc,
        registrationTime = createTime
    )
}