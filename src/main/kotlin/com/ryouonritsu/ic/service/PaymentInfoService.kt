package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.PaymentInfoDTO
import com.ryouonritsu.ic.domain.protocol.request.AddPaymentRequest
import com.ryouonritsu.ic.domain.protocol.response.ListResponse
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @author ryouonritsu
 */
interface PaymentInfoService {
    fun create(request: AddPaymentRequest): Response<PaymentInfoDTO>
    fun list(
        ids: List<Long>?,
        userId: Long?,
        rentalId: Long?,
        roomId: Long?,
        page: Int,
        limit: Int
    ): Response<ListResponse<PaymentInfoDTO>>

    fun queryStatusByUserId(userId: Long?): Response<Map<String, Any>>
}