package com.ryouonritsu.ic.common.utils

import com.ryouonritsu.ic.common.constants.ICConstant.ROOM
import com.ryouonritsu.ic.common.constants.ICConstant.USER
import com.ryouonritsu.ic.entity.Room
import com.ryouonritsu.ic.entity.User

/**
 * @author ryouonritsu
 */
object RequestContext {
    var user
        get() = ThreadLocalContext[USER] as? User
        set(value) {
            ThreadLocalContext[USER] = value
        }

    var room
        get() = ThreadLocalContext[ROOM] as? Room
        set(value) {
            ThreadLocalContext[ROOM] = value
        }
}