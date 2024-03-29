package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.protocol.request.AdminModifyMRORequest
import com.ryouonritsu.ic.domain.protocol.request.CreateMRORequest
import com.ryouonritsu.ic.domain.protocol.request.WorkerModifyMRORequest
import com.ryouonritsu.ic.domain.protocol.response.ListMROResponse
import com.ryouonritsu.ic.domain.protocol.response.ListWorkerResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import java.time.Month

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
        mroStatus: Int?,
        keyword: String?,
        label: String?,
        history: Boolean,
        page: Int,
        limit: Int
    ): Response<ListMROResponse>

    fun createMRO(request: CreateMRORequest): Response<Unit>

    fun adminModifyMRO(request: AdminModifyMRORequest): Response<Unit>

    fun workerModifyMRO(request: WorkerModifyMRORequest): Response<Unit>

    fun selectWorker(
        actualDate: String?, actualTime: String?, label: String?
    ): Response<ListWorkerResponse>

    fun statistics(year: Int): Response<Map<Month, Map<String, Int>>>
}