package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.RentalInfoDTO
import com.ryouonritsu.ic.domain.protocol.request.CreateRentalInfoRequest
import com.ryouonritsu.ic.domain.protocol.response.ListRentalInfoResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import java.time.LocalDate

interface RentalInfoService {
    fun list(
        ids: List<Long>?,
        userId: Long?,
        roomId: Long?,
        startTime: LocalDate?,
        endTime: LocalDate?,
        page: Int,
        limit: Int
    ): Response<ListRentalInfoResponse>

    fun createRentalInfo(request: CreateRentalInfoRequest): Response<RentalInfoDTO>
}