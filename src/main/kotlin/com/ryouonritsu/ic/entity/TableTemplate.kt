package com.ryouonritsu.ic.entity

import com.alibaba.fastjson2.JSONArray
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author ryouonritsu
 */
@Entity
class TableTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '模板ID'", nullable = false)
    var id: Long = 0,
    @Column(
        name = "template_type",
        columnDefinition = "TINYINT(3) DEFAULT '0' COMMENT '模板类型'",
        nullable = false
    )
    var templateType: Int = 0,
    @Column(
        name = "template_name",
        columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '模板名称'",
        nullable = false
    )
    var templateName: String = "",
    @Column(
        name = "table_name",
        columnDefinition = "VARCHAR(255) DEFAULT '' COMMENT '表名'",
        nullable = false
    )
    var tableName: String = "",
    @Column(name = "template_info", columnDefinition = "LONGTEXT COMMENT '模板信息JSON'")
    var templateInfo: String = JSONArray().toJSONString(),
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