package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.constants.TemplateType
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.RedisUtils
import com.ryouonritsu.ic.common.utils.RedisUtils.Companion.log
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
import com.ryouonritsu.ic.repository.UserRepository
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
import javax.persistence.criteria.Predicate
import kotlin.jvm.optionals.getOrElse

/**
 * @author PaulManstein
 */
@Service
class RoomServiceImpl(
    private val redisUtils: RedisUtils,
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val tableTemplateService: TableTemplateService
) : RoomService {
    override fun selectRoomById(roomId: Long): Response<RoomDTO> {
        val room = roomRepository.findById(roomId).getOrElse {
            throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        }
//        return Response.success("获取成功", room.toDTO())
        return Response.success("获取成功", RoomDTO.from(room))
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRED)
    override fun modifyRoomInfo(request: ModifyRoomInfoRequest): Response<Unit> {
        val room = roomRepository.findById(request.id!!).getOrElse {
            throw ServiceException(ExceptionEnum.OBJECT_DOES_NOT_EXIST)
        }
        if (request.userId != null) room.userId = request.userId!!
        if (request.status != null) room.status = request.status!!
        if (request.commence != null) room.commence = request.commence!!
        if (request.terminate != null) room.terminate = request.terminate!!
        if (request.contract != null) room.contract = request.contract!!
        if (!request.roomInfo.isNullOrBlank()) room.roomInfo = request.roomInfo!!
        roomRepository.save(room)
        return Response.success("修改成功")
    }

    override fun queryHeaders(): Response<List<ColumnDSL>> {
        return Response.success(tableTemplateService.queryHeaders(TemplateType.ROOM_LIST_TEMPLATE))
    }

    override fun list(
        id: Long?,
        userId: Long?,
        status: Boolean?,
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
            if (userId != null) predicates += cb.equal(root.get<Long>("userId"), userId)
            if (status != null) predicates += cb.equal(root.get<Boolean>("status"), status)
            if (commence != null) predicates += cb.greaterThanOrEqualTo(root["commence"], commence)
            if (terminate != null) predicates += cb.lessThanOrEqualTo(root["terminate"], terminate)
            if (contract != null) predicates += cb.equal(root.get<Long>("contract"), contract)
            if (!roomInfo.isNullOrBlank()) predicates += cb.like(root["roomInfo"], "%$roomInfo%")
            query.where(*predicates.toTypedArray())
                .orderBy(cb.asc(root.get<Long>("id")))
                .restriction
        }
        val result = roomRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val rooms = result.content.map { RoomDTO.from(it) }
        return Response.success(ListRoomResponse(total, rooms))
    }

    override fun download(): XSSFWorkbook {
        val headers = queryHeaders().data ?: run {
            log.error("[RoomServiceImpl.download] 没有用户列表模板")
            throw ServiceException(ExceptionEnum.TEMPLATE_NOT_EXIST)
        }
//        val data = roomRepository.findAll().map { it.toDTO() }
        val data = roomRepository.findAll().map { RoomDTO.from(it) }
        return XSSFWorkbook().process(headers, data)
    }

    override fun downloadTemplate(): XSSFWorkbook {
        val excelSheetDefinitions = getExcelSheetDefinitions()
        return XSSFWorkbook().getTemplate(excelSheetDefinitions, listOf<Room>())
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

    fun RoomDTO.Companion.from(room: Room): RoomDTO {
        val id = room.userId!!
        val user = userRepository.findByIdAndStatus(id, true)!!
        return RoomDTO(
                id = room.id.toString(),
                user = user.toDTO(),
                status = room.status,
                commence = room.commence,
                terminate = room.terminate,
                contract = room.contract.toString(),
                roomInfo = room.roomInfo
                )
    }
}