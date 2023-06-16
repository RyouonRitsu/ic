package com.ryouonritsu.ic.manager.db

import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @author ryouonritsu
 */
interface UserManager {
    fun retrySendEmail(
        email: String,
        subject: String,
        html: String,
        times: Int = 3
    ): Boolean

    fun sendEmail(email: String, subject: String, html: String): Boolean
    fun verifyCodeCheck(email: String, verifyCode: String?): Pair<Boolean, Response<Unit>?>
}