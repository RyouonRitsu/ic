package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.protocol.request.CreateRentalInfoRequest
import com.ryouonritsu.ic.domain.protocol.response.ListRentalInfoResponse
import com.ryouonritsu.ic.domain.protocol.response.Response

interface RentalInfoService {

    fun list(
        id: String?, customId: String?, roomId :String?,page :Int, limit :Int

    ): Response<ListRentalInfoResponse>

    fun createRentalInfo(request: CreateRentalInfoRequest): Response<Unit>

//    fun adminModifyRentalInfo(request: AdminModifyRentalInfoRequest): Response<Unit>
}