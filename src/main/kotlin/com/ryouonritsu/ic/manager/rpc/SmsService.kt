package com.ryouonritsu.ic.manager.rpc

/**
 * @author ryouonritsu
 */
interface SmsService {
    fun sendVerifyCodeSms(phone: String, code: String): Boolean
}