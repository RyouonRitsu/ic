package com.ryouonritsu.ic.service.impl

import com.alibaba.fastjson2.parseArray
import com.alibaba.fastjson2.toJSONString
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.domain.dto.RentalInfoDTO
import com.ryouonritsu.ic.domain.protocol.request.CreateRentalInfoRequest
import com.ryouonritsu.ic.domain.protocol.response.ListRentalInfoResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.RentalInfo
import com.ryouonritsu.ic.repository.RentalInfoRepository
import com.ryouonritsu.ic.repository.RoomRepository
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.RentalInfoService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate
import kotlin.jvm.optionals.getOrElse

@Service
class RentalInfoServiceImpl(
    private val rentalInfoRepository: RentalInfoRepository,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val transactionTemplate: TransactionTemplate
) : RentalInfoService {
    companion object {
        private val log = LoggerFactory.getLogger(RentalInfoServiceImpl::class.java)
    }

    override fun list(
        ids: List<Long>?,
        userId: Long?,
        roomId: Long?,
        startTime: LocalDate?,
        endTime: LocalDate?,
        page: Int,
        limit: Int
    ): Response<ListRentalInfoResponse> {
        val specification = Specification<RentalInfo> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!ids.isNullOrEmpty()) predicates += cb.`in`(root.get<Long>("id")).apply {
                ids.forEach { this.value(it) }
            }
            if (userId != null) predicates += cb.equal(root.get<Long>("userId"), userId)
            if (roomId != null) predicates += cb.equal(root.get<Long>("roomId"), roomId)
            if (startTime != null)
                predicates += cb.greaterThanOrEqualTo(root["startTime"], startTime)
            if (endTime != null) predicates += cb.lessThanOrEqualTo(root["endTime"], endTime)
            predicates += cb.equal(root.get<Boolean>("status"), true)
            query.where(*predicates.toTypedArray())
                .orderBy(cb.asc(root.get<LocalDateTime>("createTime")))
                .restriction
        }
        val result = rentalInfoRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val list = result.content.map { it.toDTO() }
        return Response.success(ListRentalInfoResponse(total, list))
    }

    override fun createRentalInfo(request: CreateRentalInfoRequest): Response<RentalInfoDTO> {
        val user = userRepository.findByIdAndStatus(request.userId!!)
            ?: throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        val room = roomRepository.findById(request.roomId!!).getOrElse {
            throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        }
        if (room.status) return Response.failure("房间已被租赁")

        var rentalInfo = RentalInfo(
            userId = request.userId,
            roomId = request.roomId,
            startTime = request.startTime!!,
            endTime = request.endTime!!,
            totalCost = request.totalCost!!,
        )
        room.userId = request.userId
        room.commence = request.startTime
        room.terminate = request.endTime
        room.status = true
        transactionTemplate.execute {
            rentalInfo = rentalInfoRepository.save(rentalInfo)
            log.info("[RentalInfoServiceImpl.createRentalInfo] save successful, id = ${rentalInfo.id}")
            user.rentalInfoIds = user.rentalInfoIds.parseArray<Long>()
                .apply { this += rentalInfo.id }
                .toJSONString()
            log.info("[RentalInfoServiceImpl.createRentalInfo] now user's rentalInfoIds = ${user.rentalInfoIds}")
            userRepository.save(user)
            roomRepository.save(room)
        }
        return Response.success(rentalInfo.toDTO())
    }
}