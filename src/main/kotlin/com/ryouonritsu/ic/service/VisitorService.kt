package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.VisitorDTO
import com.ryouonritsu.ic.domain.protocol.request.CreateVisitorRequest
import com.ryouonritsu.ic.domain.protocol.response.ListResponse
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @Author Kude
 * @Date 2023/6/26 11:02
 */
interface VisitorService {
    fun createVisitor(request: CreateVisitorRequest): Response<VisitorDTO>

    fun statisticsDay(date: String): Response<Map<String, List<Any>>>

    fun statisticsMonth(year: Int): Response<Map<String, List<Any>>>

    fun statisticsCompany(): Response<List<Map<String, Any>>>

    fun list(
        ids: List<Long>?,
        userId: Long?,
        page: Int,
        limit: Int
    ): Response<ListResponse<VisitorDTO>>
}