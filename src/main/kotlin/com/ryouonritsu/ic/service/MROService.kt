package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.MRODTO
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @Author Kude
 * @Date 2023/6/16 14:25
 */
interface MROService {
    fun selectMROByUserId(userId: Long): Response<List<MRODTO>>
}