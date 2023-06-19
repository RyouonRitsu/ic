package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.domain.dto.PaymentDTO
import com.ryouonritsu.ic.domain.protocol.request.AddPaymentRequest
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.Payment
import com.ryouonritsu.ic.repository.PaymentRepository
import com.ryouonritsu.ic.service.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * @author PaulManstein
 */
@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
) : PaymentService {
    companion object{
        private val log = LoggerFactory.getLogger(PaymentServiceImpl::class.java)
    }

    override fun addPayment(request: AddPaymentRequest): Response<Unit> {
        paymentRepository.save(
            Payment(
                rentid = request.rentid!!,
                roomid = request.roomid!!,
                paytime = request.paytime!!,
                expense = request.expense!!
            )
        )
        return Response.success("增加缴费单成功")
    }

    override fun findByPaymentId(paymentId: Long): Response<List<PaymentDTO>> {
        TODO("Not yet implemented")
    }

    override fun findByRoomId(roomId: Long): Response<List<PaymentDTO>> {
        TODO("Not yet implemented")
    }

    override fun findByUserId(userId: Long): Response<List<PaymentDTO>> {
        TODO("Not yet implemented")
    }
}