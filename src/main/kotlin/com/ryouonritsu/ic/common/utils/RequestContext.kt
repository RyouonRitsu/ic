package com.ryouonritsu.ic.common.utils

import com.ryouonritsu.ic.common.constants.ICConstant.USER_ID

/**
 * @author ryouonritsu
 */
object RequestContext {
    var userId
        get() = ThreadLocalContext[USER_ID] as? Long
        set(value) {
            ThreadLocalContext[USER_ID] = value
        }
}