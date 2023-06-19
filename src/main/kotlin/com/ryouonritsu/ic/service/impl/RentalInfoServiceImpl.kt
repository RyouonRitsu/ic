package com.ryouonritsu.ic.service.impl


//import org.hibernate.annotations.common.util.impl.LoggerFactory
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.protocol.request.CreateRentalInfoRequest
import com.ryouonritsu.ic.domain.protocol.response.ListRentalInfoResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.RentalInfo
import com.ryouonritsu.ic.repository.RentalInfoRepository
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.RentalInfoService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import javax.persistence.criteria.Predicate



@Service
class RentalInfoServiceImpl (
    private val rentalInfoRepository: RentalInfoRepository,
    private val userRepository: UserRepository,
    private val redisUtils: RedisUtils,
) : RentalInfoService {

    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)

    }

    override fun list(
        id: String?,
        customId: String?,
        roomId: String?,
        page: Int,
        limit: Int
    ): Response<ListRentalInfoResponse> {
        val specification = Specification<RentalInfo> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!id.isNullOrBlank()) {
                predicates += cb.equal(root.get<Long>("id"), id)
            }
            if (!customId.isNullOrBlank()) {
                predicates += cb.equal(root.get<Long>("customId"), customId)
            }
            if (!roomId.isNullOrBlank()) {
                predicates += cb.equal(root.get<Long>("roomId"), roomId)
            }
            query.where(*predicates.toTypedArray()).restriction

        }

        var result = rentalInfoRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val infos=result.content.map{ it.toDTO()}
        return Response.success(ListRentalInfoResponse(total,infos))


    }



    @org.springframework.transaction.annotation.Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun createRentalInfo(request: CreateRentalInfoRequest): Response<Unit> {
        val user=userRepository.findByIdAndStatus(RequestContext.user!!.id)
            ?: throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        rentalInfoRepository.save(
            RentalInfo(
                customId = user.id,
                roomId = request.roomId!!,
                startTime = request.startTime,
                endTime = request.endTime,
                totalCost = request.totalCost,
            )
        )
        return Response.success()
    }


}