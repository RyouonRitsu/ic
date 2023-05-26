package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.manager.db.UserManager
import com.ryouonritsu.ic.repository.UserRepository
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrElse

/**
 * @author ryouonritsu
 */
class UserManagerImpl(
    private val userRepository: UserRepository,
    private val transactionTemplate: TransactionTemplate
) : UserManager {
    override fun adjustProperty(userId: Long, value: BigDecimal) {
        val user = userRepository.findById(userId).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        user.property += value
        if (user.property < BigDecimal.ZERO) throw ServiceException(ExceptionEnum.DATA_ERROR)
        transactionTemplate.execute { userRepository.save(user) }
    }
}