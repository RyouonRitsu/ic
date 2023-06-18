package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.protocol.request.AdminModifyMRORequest
import com.ryouonritsu.ic.domain.protocol.request.CreateMRORequest
import com.ryouonritsu.ic.domain.protocol.response.ListMROResponse
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @Author Kude
 * @Date 2023/6/16 14:25
 */
interface MROService {
    fun list(
        id: String?, customId: String?, workerId: String?, roomId: String?, isSolved: Boolean?, page: Int, limit: Int
    ): Response<ListMROResponse>

    fun createMRO(request: CreateMRORequest): Response<Unit>

    fun adminModifyMRO(request: AdminModifyMRORequest): Response<Unit>
}