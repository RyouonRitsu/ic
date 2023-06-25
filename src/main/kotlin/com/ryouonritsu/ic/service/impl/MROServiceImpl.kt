package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.domain.protocol.request.*
import com.ryouonritsu.ic.domain.protocol.response.ListMROResponse
import com.ryouonritsu.ic.domain.protocol.response.ListWorkerResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.MRO
import com.ryouonritsu.ic.entity.User
import com.ryouonritsu.ic.manager.db.NotificationManager
import com.ryouonritsu.ic.repository.MRORepository
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.MROService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.Month
import javax.persistence.criteria.Predicate

/**
 * @Author Kude
 * @Date 2023/6/16 14:32
 */
@Service
class MROServiceImpl(
    private val redisUtils: RedisUtils,
    private val mroRepository: MRORepository,
    private val userRepository: UserRepository,
    private val notificationManager: NotificationManager
) : MROService {
    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    fun getOccupiedWorkers(actualDate: String?, actualTime: String?): List<MRO> {
        val specification = Specification<MRO> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!actualDate.isNullOrBlank()) {
                predicates += cb.equal(root.get<String>("actualDate"), actualDate)
            }
            if (!actualTime.isNullOrBlank()) {
                predicates += cb.equal(root.get<String>("actualTime"), actualTime)
            }
            predicates += cb.equal(root.get<Boolean>("status"), true)
            query.where(*predicates.toTypedArray()).restriction
        }
        return mroRepository.findAll(specification)
    }

    fun getUserType(label: String?): User.UserType {
        return if (label.equals("water")) {
            User.UserType.WATER_MAINTENANCE_STAFF
        } else if (label.equals("electric")) {
            User.UserType.ELECTRICITY_MAINTENANCE_STAFF
        } else {
            User.UserType.MACHINE_MAINTENANCE_STAFF
        }
    }

    fun getMsg(user: User, mro: MRO): String {
        return "{\"mroId\":\"${mro.id}\"," +
                "\"problem\":\"${mro.problem}\"," +
                "\"userAvatar\":\"${user.avatar}\"," +
                "\"username\":\"${user.username}\"}"
    }

    fun getUserMsg(mro: MRO): String {
        return "{\"mroId\":\"${mro.id}\"," +
                "\"problem\":\"${mro.problem}\"," +
                "\"status\":\"${mro.status}\"}"
    }

    override fun list(
        id: String?,
        customId: String?,
        workerId: String?,
        roomId: String?,
        mroStatus: Int?,
        keyword: String?,
        label: String?,
        page: Int,
        limit: Int
    ): Response<ListMROResponse> {
        return runCatching {
            val specification = Specification<MRO> { root, query, cb ->
                val predicates = mutableListOf<Predicate>()
                if (!id.isNullOrBlank()) {
                    predicates += cb.equal(root.get<Long>("id"), id)
                }
                if (!customId.isNullOrBlank()) {
                    predicates += cb.equal(root.get<Long>("customId"), customId)
                }
                if (!workerId.isNullOrBlank()) {
                    predicates += cb.equal(root.get<Long>("workerId"), workerId)
                }
                if (!roomId.isNullOrBlank()) {
                    predicates += cb.equal(root.get<Long>("roomId"), roomId)
                }
                if (mroStatus != null) {
                    predicates += cb.equal(root.get<Int>("mroStatus"), mroStatus)
                }
                if (!keyword.isNullOrBlank()) {
                    predicates += cb.or(
                        cb.like(root.get("problem"), "%$keyword%"),
                        cb.like(root.get("resolvent"), "%$keyword%")
                    )
                }
                if (!label.isNullOrBlank()) {
                    predicates += cb.equal(root.get<String>("label"), label)
                }
                predicates += cb.equal(root.get<Boolean>("status"), true)
                query.where(*predicates.toTypedArray())
                    .orderBy(cb.asc(root.get<Int>("mroStatus")))
                    .restriction
            }
            val result = mroRepository.findAll(specification, PageRequest.of(page - 1, limit))
            val total = result.totalElements
            val orders = result.content.map { it.toDTO() }
            orders.forEach {
                it.userInfo = userRepository.findById(it.customId.toLong()).get().toMROUserInfoDTO()
                if (it.workerId.toLong() != 0L) {
                    it.workerInfo =
                        userRepository.findById(it.workerId.toLong()).get().toMROUserInfoDTO()
                }
            }
            Response.success(ListMROResponse(total, orders))
        }.onFailure {
            if (it is NoSuchElementException) {
                return Response.failure("查询不到用户ID所对应用户")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("查询失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun createMRO(request: CreateMRORequest): Response<Unit> {
        return runCatching {
            val user = userRepository.findById(RequestContext.user!!.id).get()
            val userType = getUserType(request.label)
            val userIdList =
                userRepository.findAllByUserTypeAndStatus(userType.code).map { it.id }
            val workerList = getOccupiedWorkers(request.actualDate, request.actualTime)
                .groupBy { it.workerId }
            val workerIdList = workerList.keys
            val res = userIdList.toSet().filter { !workerIdList.contains(it) }
            val workerId = res.firstOrNull() ?: workerList
                .filter { userIdList.contains(it.key) }
                .minBy { it.value.size }
                .key
            var mro = MRO(
                customId = user.id,
                problem = request.problem!!,
                expectTime = request.expectTime ?: "",
                actualDate = request.actualDate!!,
                actualTime = request.actualTime!!,
                workerId = workerId,
                label = request.label!!,
                roomId = request.roomId!!,
            )
            mro = mroRepository.save(mro)
            val msg = getMsg(user, mro)
            val adminIdList =
                userRepository.findAllByUserTypeAndStatus(User.UserType.ADMIN.code).map { it.id }
            notificationManager.batchPublish(
                BatchPublishRequest(adminIdList, "MRO_admin", msg, false)
            )
            Response.success<Unit>("创建成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("创建失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun adminModifyMRO(request: AdminModifyMRORequest): Response<Unit> {
        return runCatching {
            userRepository.findById(RequestContext.user!!.id).get()
            val mro = mroRepository.findByIdAndStatus(request.id!!.toLong())
                ?: return Response.failure("维修工单不存在")
            mro.workerId = request.workerId!!.toLong()
            if (!request.actualDate.isNullOrBlank()) {
                mro.actualDate = request.actualDate!!
            }
            if (!request.actualTime.isNullOrBlank()) {
                mro.actualTime = request.actualTime!!
            }
            mro.mroStatus = 1
            val customMsg = getUserMsg(mro)
            notificationManager.publish(
                PublishRequest(mro.customId, "MRO_feedback", customMsg, false)
            )
            val user = userRepository.findById(mro.customId).get()
            val workerMsg = getMsg(user, mro)
            notificationManager.publish(
                PublishRequest(mro.workerId, "MRO_notice", workerMsg, false)
            )
            mroRepository.save(mro)
            Response.success<Unit>("修改成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("修改失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun workerModifyMRO(request: WorkerModifyMRORequest): Response<Unit> {
        return runCatching {
            userRepository.findById(RequestContext.user!!.id).get()
            val mro = mroRepository.findByIdAndStatus(request.id!!.toLong())
                ?: return Response.failure("维修工单不存在")
            mro.resolvent = request.resolvent!!
            mro.maintenanceTime = request.maintenanceTime!!
            mro.mroStatus = 2
            mroRepository.save(mro)
            val msg = getUserMsg(mro)
            notificationManager.publish(PublishRequest(mro.customId, "MRO_finish", msg, false))
            Response.success<Unit>("修改成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("修改失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun selectWorker(
        actualDate: String?,
        actualTime: String?,
        label: String?
    ): Response<ListWorkerResponse> {
        return runCatching {
            val userType = getUserType(label)
            val userList =
                userRepository.findAllByUserTypeAndStatus(userType.code).map { it.toDTO() }
            val workers = getOccupiedWorkers(actualDate, actualTime).map { it.workerId }
            val workerList = workers.map { userRepository.findByIdAndStatus(it)!!.toDTO() }
            val res = userList.toSet().filter { !workers.contains(it.id.toLong()) } + workerList
            val total = res.size
            Response.success(ListWorkerResponse(total, res))
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("查询失败, 发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun statistics(year: Int): Response<Map<Month, Map<String, Int>>> {
        return runCatching {
            userRepository.findById(RequestContext.user!!.id).get()
            val specification = Specification<MRO> { root, query, cb ->
                val predicates = mutableListOf<Predicate>()
                predicates += cb.greaterThanOrEqualTo(
                    root.get("createTime"),
                    LocalDateTime.of(year, 1, 1, 0, 0, 0)
                )
                predicates += cb.lessThanOrEqualTo(
                    root.get("createTime"),
                    LocalDateTime.of(year, 12, 31, 23, 59, 59)
                )
                predicates += cb.equal(root.get<Boolean>("status"), true)
                query.where(*predicates.toTypedArray())
                    .orderBy(cb.asc(root.get<Int>("mroStatus")))
                    .restriction
            }
            val mroList = mroRepository.findAll(specification)
                .groupBy { it.createTime.month }
                .mapValues { it.value.groupBy { it.label }.mapValues { it.value.size } }
            Response.success<Map<Month, Map<String, Int>>>("查询成功", mroList)
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.user!!.id}"
                return Response.failure("数据库中没有此用户或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("查询失败, 发生意外错误"))
    }
}