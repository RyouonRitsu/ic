package com.ryouonritsu.ic.common.constants

import com.ryouonritsu.ic.entity.User

/**
 * @author ryouonritsu
 */
object ICConstant {
    /**
     * Int型0
     */
    const val INT_0 = 0

    /**
     * Int型1
     */
    const val INT_1 = 1

    /**
     * Int型2
     */
    const val INT_2 = 2

    /**
     * Int型3
     */
    const val INT_3 = 3

    /**
     * Int型256
     */
    const val INT_256 = 256

    /**
     * Int型184
     */
    const val INT_184 = 184

    /**
     * Int型20000
     */
    const val INT_20000 = 20000

    /**
     * Int型40000
     */
    const val INT_40000 = 40000

    /**
     * Int型65280
     */
    const val INT_65280 = 65280

    /**
     * Long型-1
     */
    const val LONG_MINUS_1 = -1L

    /**
     * Long型0
     */
    const val LONG_0 = 0L

    /**
     * 空字符串
     */
    const val EMPTY_STR = ""

    /**
     * 字串0
     */
    const val STR_0 = "0"

    /**
     * 字符串中划线
     */
    const val MIDDLE_LINE_STR = "-"

    /**
     * 成功
     */
    const val SUCCESS = "success"

    /**
     * 事件
     */
    const val EVENT = "event"

    /**
     * 用户ID
     */
    const val USER_ID = "userId"

    /**
     * 用户
     */
    const val USER = "user"

    /**
     * 房间
     */
    const val ROOM = "Room"

    /**
     * 唯一键
     */
    const val UNIQUE = "unique"

    /**
     * 必填
     */
    const val REQUIRED = "required"

    /**
     * 默认分隔符
     */
    const val DEFAULT_DELIMITER = ", "

    /**
     * 维修人员类型集合
     */
    val MAINTENANCE_STAFF_TYPE_CODES = setOf(
        User.UserType.WATER_MAINTENANCE_STAFF(),
        User.UserType.ELECTRICITY_MAINTENANCE_STAFF(),
        User.UserType.MACHINE_MAINTENANCE_STAFF()
    )

    /**
     * 房间ID
     */
    const val ROOM_ID = "roomId"

    /**
     * Region ID
     */
    const val SMS_REGION = "cn-beijing"

    /**
     * EndpointOverride
     */
    const val SMS_API = "dysmsapi.aliyuncs.com"

    const val OK = "OK"

    const val VERIFICATION_CODE_SEND_SUCCESSFUL = "验证码已发送"

    const val VERIFICATION_CODE_SEND_FAILED = "验证码发送失败"
}