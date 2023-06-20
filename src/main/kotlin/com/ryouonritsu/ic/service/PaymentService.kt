package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.domain.dto.PaymentDTO
import com.ryouonritsu.ic.domain.protocol.request.AddPaymentRequest
import com.ryouonritsu.ic.domain.protocol.response.Response

/**
 * @author PaulManstein
 */
interface PaymentService {
    fun addPayment(request: AddPaymentRequest): Response<Unit>
    fun findByPaymentId(paymentId: Long): Response<List<PaymentDTO>>
    fun findByUserId(userId: Long): Response<List<PaymentDTO>>
    fun findByRoomId(roomId: Long): Response<List<PaymentDTO>>
}