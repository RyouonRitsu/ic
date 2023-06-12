package com.ryouonritsu.ic.config

import com.alibaba.fastjson2.JSONObject
import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.enums.AuthEnum
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.common.utils.TokenUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author ryouonritsu
 */
@Component
class TokenInterceptor : HandlerInterceptor {
    @Autowired
    lateinit var redisUtils: RedisUtils
    val log: Logger = LoggerFactory.getLogger(TokenInterceptor::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (request.method == "OPTIONS" || handler !is HandlerMethod ||
            !handler.hasMethodAnnotation(AuthCheck::class.java) ||
            AuthEnum.TOKEN !in handler.method.getAnnotation(AuthCheck::class.java).auth
        ) {
            response.status = HttpServletResponse.SC_OK
            return true
        }
        response.characterEncoding = "UTF-8"
        val token = request.getHeader("token")
        var message = ""
        if (!token.isNullOrBlank()) {
//            if (TokenUtils.verify(token).first) {
//                log.info("通过拦截器")
//                return true
//            }
            val (result, userId) = TokenUtils.verify(token)
            log.info("现有的token: $token")
            if (redisUtils["$userId"] == token && result) {
                log.info("通过拦截器")
                RequestContext.userId = userId
                return true
            } else {
                log.info("已经存在一个token，未通过拦截器")
            }
        } else message = "无法获取token，"
        response.contentType = "application/json; charset=utf-8"
        try {
            val json = JSONObject()
            json["code"] = ExceptionEnum.UNAUTHORIZED.code
            json["message"] = "${message}验证失败"
            response.writer.append(json.toString())
            log.info("认证失败，未通过拦截器")
        } catch (e: Exception) {
            return false
        }
        return false
    }
}
