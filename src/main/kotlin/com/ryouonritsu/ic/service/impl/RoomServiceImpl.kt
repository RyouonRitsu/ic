package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.constants.ICConstant.LONG_MINUS_1
import com.ryouonritsu.ic.common.constants.TemplateType
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RedisUtils.Companion.log
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.component.file.ExcelSheetDefinition
import com.ryouonritsu.ic.component.file.converter.RoomUploadConverter
import com.ryouonritsu.ic.component.getTemplate
import com.ryouonritsu.ic.component.process
import com.ryouonritsu.ic.component.read
import com.ryouonritsu.ic.domain.dto.RoomDTO
import com.ryouonritsu.ic.domain.protocol.request.ModifyRoomInfoRequest
import com.ryouonritsu.ic.domain.protocol.response.ListRoomResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.Room
import com.ryouonritsu.ic.repository.RoomRepository
import com.ryouonritsu.ic.service.RoomService
import com.ryouonritsu.ic.service.TableTemplateService
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.persistence.criteria.Predicate

/**
 * @author PaulManstein
 */
@Service
class RoomServiceImpl(
    private val redisUtils: RedisUtils,
    private val roomRepository: RoomRepository,
    private val tableTemplateService: TableTemplateService
) : RoomService {
    override fun showInfo(roomId: Long): Response<RoomDTO> {
        return runCatching {
            val room = roomRepository.findById(roomId).get()
            Response.success("获取成功", room.toDTO())
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "$roomId"
                return Response.failure("数据库中没有此房间，此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(
            Response.failure("获取失败，发生意外错误")
        )
    }

    override fun selectRoomById(roomId: Long): Response<List<RoomDTO>> {
        return runCatching {
            val room = roomRepository.findById(roomId).get()
            Response.success("获取成功", listOf(room.toDTO()))
        }.onFailure {
            if (it is NoSuchElementException) return Response.failure("数据库中没有此房间")
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("获取失败，发生意外错误"))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun modifyRoomInfo(request: ModifyRoomInfoRequest): Response<Unit> {
        return runCatching {
            val room = roomRepository.findById(request.id ?: RequestContext.room!!.id).get()
            if (request.userId != LONG_MINUS_1) room.userId = request.userId!!
            if (request.status != LONG_MINUS_1) room.status = request.status!!
            if (!request.commence.isNullOrBlank()) {
                try {
                    room.commence =
                        LocalDate.parse(request.commence, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    return Response.failure("租赁开始格式错误，应为yyyy-MM-dd")
                }
            }
            if (!request.terminate.isNullOrBlank()) {
                try {
                    room.terminate =
                        LocalDate.parse(
                            request.terminate, DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        )
                } catch (e: Exception) {
                    return Response.failure("租赁开始格式错误，应为yyyy-MM-dd")
                }
            }
            if (request.contract != LONG_MINUS_1) room.contract = request.contract!!
            if (!request.roomInfo.isNullOrBlank()) room.roomInfo = request.roomInfo!!
            roomRepository.save(room)
            Response.success<Unit>("修改成功")
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${RequestContext.room!!.id}"
                return Response.failure("数据库中没有此房间或可能是token验证失败, 此会话已失效")
            }
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("修改失败，发生意外错误"))
    }

    override fun queryHeaders(): Response<List<ColumnDSL>> {
        return Response.success(tableTemplateService.queryHeaders(TemplateType.ROOM_LIST_TEMPLATE))
    }

    override fun list(
        id: Long?,
        userId: Long?,
        status: Long?,
        commence: LocalDate?,
        terminate: LocalDate?,
        contract: Long?,
        roomInfo: String?,
        page: Int,
        limit: Int
    ): Response<ListRoomResponse> {
        val specification = Specification<Room> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (id != null) predicates += cb.equal(root.get<Long>("id"), id)
            if (userId != null) predicates += cb.equal(root.get<Long>("userId"), id)
            if (status != null) predicates += cb.equal(root.get<Long>("status"), id)
            if (commence != null) predicates += cb.equal(root.get<LocalDate>("commence"), commence)
            if (terminate != null)
                predicates += cb.equal(root.get<LocalDate>("terminate"), terminate)
            if (contract != null) predicates += cb.equal(root.get<Long>("contract"), contract)
            if (!roomInfo.isNullOrBlank()) predicates += cb.like(root["roomInfo"], "%$roomInfo")
            query.where(*predicates.toTypedArray())
                .orderBy(cb.asc(root.get<Long>("id")))
                .restriction
        }
        val result = roomRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val rooms = result.content.map { it.toDTO() }
        return Response.success(ListRoomResponse(total, rooms))
    }

    override fun download(): XSSFWorkbook {
        val headers = queryHeaders().data ?: run {
            log.error("[RoomServiceImpl.download] 没有用户列表模板")
            throw ServiceException(ExceptionEnum.TEMPLATE_NOT_EXIST)
        }
        val data = roomRepository.findAll().map { it.toDTO() }
        return XSSFWorkbook().process(headers, data)
    }

    override fun downloadTemplate(): XSSFWorkbook {
        val excelSheetDefinitions = getExcelSheetDefinitions()
        val room = RoomDTO(
            status = "0"
        )
        return XSSFWorkbook().getTemplate(excelSheetDefinitions, listOf(room))
    }

    private fun getExcelSheetDefinitions(): List<ExcelSheetDefinition> {
        return tableTemplateService.queryExcelSheetDefinitions(TemplateType.ROOM_UPLOAD_TEMPLATE)
    }

    /*override fun findByKeyword(keyword: String): Response<List<RoomDTO>> {
        TODO("Not yet implemented")
    }*/

    override fun upload(file: MultipartFile): Response<Unit> {
        val excelSheetDefinitions = getExcelSheetDefinitions()
        val rooms = file.read(excelSheetDefinitions, RoomUploadConverter::convert)
        roomRepository.saveAll(rooms)
        return Response.success("上传成功")
    }
}