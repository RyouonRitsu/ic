package com.ryouonritsu.ic.manager.db

import java.math.BigDecimal

/**
 * @author ryouonritsu
 */
interface UserManager {
    fun adjustProperty(userId: Long, value: BigDecimal)
}