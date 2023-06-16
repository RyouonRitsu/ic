package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.manager.db.UserManager
import com.ryouonritsu.ic.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * @author ryouonritsu
 */
@Component
class UserManagerImpl(
    private val userRepository: UserRepository,
    private val transactionTemplate: TransactionTemplate
) : UserManager {
}