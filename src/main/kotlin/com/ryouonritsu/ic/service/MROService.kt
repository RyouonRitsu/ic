package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.MRODTO
import com.ryouonritsu.ic.domain.protocol.response.ListMROResponse
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @Author Kude
 * @Date 2023/6/16 14:25
 */
interface MROService {
    fun list(
        id: String?,
        customId: String?,
        workerId: String?,
        roomId: String?,
        isSolved: Boolean?,
        page: Int,
        limit: Int
    ): Response<ListMROResponse>
}