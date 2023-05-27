package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.entity.User
import com.ryouonritsu.ic.manager.db.UserManager
import com.ryouonritsu.ic.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrElse

/**
 * @author ryouonritsu
 */
@Component
class UserManagerImpl(
    private val userRepository: UserRepository,
    private val transactionTemplate: TransactionTemplate
) : UserManager {
    override fun adjustProperty(userId: Long, value: BigDecimal) {
        val user = userRepository.findById(userId).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        adjustProperty(user, value)
    }

    override fun adjustProperty(user: User, value: BigDecimal) {
        user.property += value
        if (user.property < BigDecimal.ZERO) throw ServiceException(ExceptionEnum.INSUFFICIENT_BALANCE)
        transactionTemplate.execute { userRepository.save(user) }
    }
}