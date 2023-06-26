package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.protocol.request.CreateVisitorRequest
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @Author Kude
 * @Date 2023/6/26 11:02
 */
interface VisitorService {
    fun createVisitor(request: CreateVisitorRequest): Response<Unit>
}