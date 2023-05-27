package com.ryouonritsu.ic.manager.db

import com.ryouonritsu.ic.entity.User
import java.math.BigDecimal

/**
 * @author ryouonritsu
 */
interface UserManager {
    fun adjustProperty(userId: Long, value: BigDecimal)
    fun adjustProperty(user: User, value: BigDecimal)
}