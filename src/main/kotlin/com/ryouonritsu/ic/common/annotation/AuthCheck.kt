package com.ryouonritsu.ic.common.annotation

import com.ryouonritsu.ic.common.constants.ICConstant.MAINTENANCE_STAFF_TYPE_CODES
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.entity.User
import com.ryouonritsu.ic.repository.UserRepository
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author ryouonritsu
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AuthCheck(
    val auth: Array<AuthEnum> = [AuthEnum.TOKEN]
)

/**
 * @author ryouonritsu
 */
@Aspect
@Component
class AuthCheckAspect(
    private val userRepository: UserRepository
) {
    companion object {
        private val log = LoggerFactory.getLogger(AuthCheckAspect::class.java)
    }

    @Pointcut("@annotation(com.ryouonritsu.ic.common.annotation.AuthCheck)")
    private fun authCheck() {
    }

    @Before("authCheck()")
    @Throws(Throwable::class)
    fun before(joinPoint: JoinPoint) {
        val authCheck = (joinPoint.signature as MethodSignature).method
            .getAnnotation(AuthCheck::class.java)
        val user by lazy {
            userRepository.findById(RequestContext.user!!.id)
                .orElseThrow { throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST) }
        }
        if (AuthEnum.ADMIN in authCheck.auth && user.userType != User.UserType.ADMIN()
            || AuthEnum.CLIENT in authCheck.auth && user.userType != User.UserType.CLIENT()
            || AuthEnum.MAINTENANCE_STAFF in authCheck.auth && user.userType !in MAINTENANCE_STAFF_TYPE_CODES
        ) throw ServiceException(ExceptionEnum.PERMISSION_DENIED)
    }
}