package com.ryouonritsu.ic.manager.rpc.impl

import com.alibaba.fastjson2.toJSONString
import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest
import com.ryouonritsu.ic.common.constants.ICConstant
import com.ryouonritsu.ic.manager.rpc.SmsService
import darabonba.core.client.ClientOverrideConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * @author ryouonritsu
 */
@Component
class SmsServiceImpl(
    @Value("\${alibaba.cloud.accessKey.id}")
    private val accessKeyId: String,
    @Value("\${alibaba.cloud.accessKey.secret}")
    private val accessKeySecret: String,
    @Value("\${sms.verifyCode.signName}")
    private val signName: String,
    @Value("\${sms.verifyCode.templateCode}")
    private val templateCode: String
) : SmsService {
    companion object {
        private val log = LoggerFactory.getLogger(SmsServiceImpl::class.java)
    }

    private val provider by lazy {
        StaticCredentialProvider.create(
            Credential.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build()
        )
    }

    override fun sendVerifyCodeSms(phone: String, code: String): Boolean {
        val client = AsyncClient.builder()
            .region(ICConstant.SMS_REGION)
            .credentialsProvider(provider)
            .overrideConfiguration(
                ClientOverrideConfiguration.create()
                    .setEndpointOverride(ICConstant.SMS_API)
            )
            .build()

        val request = SendSmsRequest.builder()
            .signName(signName)
            .templateCode(templateCode)
            .phoneNumbers(phone)
            .templateParam(VerifyCodeTemplateParam(code).toJSONString())
            .build()
        val response = client.sendSms(request)
        val resp = response.get()
        val result = if (resp.body.code == ICConstant.OK) {
            log.info("[SmsServiceImpl.sendVerifyCodeSms] send successful! Message = ${resp.body.message}, BizId = ${resp.body.bizId}, RequestId = ${resp.body.requestId}")
            true
        } else {
            log.error("[SmsServiceImpl.sendVerifyCodeSms] send failed! Code = ${resp.body.code}, Message = ${resp.body.message}, BizId = ${resp.body.bizId}, RequestId = ${resp.body.requestId}")
            false
        }

        client.close()
        return result
    }
}

/**
 * @author ryouonritsu
 */
data class VerifyCodeTemplateParam(
    val code: String
)