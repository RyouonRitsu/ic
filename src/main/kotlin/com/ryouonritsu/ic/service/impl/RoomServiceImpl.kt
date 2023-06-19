package com.ryouonritsu.ic.service.impl

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
import com.ryouonritsu.ic.repository.RoomRepository
import com.ryouonritsu.ic.service.RoomService
import org.springframework.stereotype.Service
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.RoomFile
import com.ryouonritsu.ic.repository.RoomFileRepository
import com.ryouonritsu.ic.service.TableTemplateService
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import java.io.File
import kotlin.io.path.Path

/**
 * @author PaulManstein
 */

@Service
class RoomServiceImpl(
    private val redisUtils: RedisUtils,
    private val roomRepository: RoomRepository,
    private val roomFileRepository: RoomFileRepository,
    private val tableTemplateService: TableTemplateService,
    @Value("\${static.file.prefix}")
    private val staticFilePrefix: String,
) : RoomService {
    override fun showInfo(roomId: Long): Response<List<RoomDTO>> {
        TODO("Not yet implemented")
    }
    override fun selectRoomById(roomId: Long): Response<List<RoomDTO>> {
        return runCatching {
            val room = roomRepository.findById(roomId).get()
            Response.success("获取成功", listOf(room.toDTO()))
        }.onFailure {
            if(it is NoSuchElementException) return Response.failure("数据库中没有此房间")
            log.error(it.stackTraceToString())
        }.getOrDefault(Response.failure("获取失败，发生意外错误"))
    }

    override fun uploadFile(file: MultipartFile): Response<List<Map<String, String>>> {
        return runCatching {
            if (file.size >= 10 * 1024 *1024) return Response.failure("上传失败，文件大小超过最大限制10MB！")
            val time = System.currentTimeMillis()
            val roomId  = RequestContext.roomId
            var fileDir = "static/file/${roomId}"
            val fileName = "${time}_${file.originalFilename}"
            val filePath = "$fileDir/$fileName"
            if (!File(fileDir).exists()) File(fileDir).mkdirs()
            file.transferTo(Path(filePath))
            val fileUrl = "http://$staticFilePrefix:8090/file/${roomId}/${fileName}"
            roomFileRepository.save(
                RoomFile(
                    url = fileUrl,
                    filePath = filePath,
                    fileName = fileName,
                    roomId = roomId!!
                )
            )
            Response.success(
                "上传成功", listOf(
                    mapOf(
                        "url" to fileUrl
                    )
                )
            )
        }.onFailure { log.error(it.stackTraceToString()) }
            .getOrDefault(Response.failure("上传失败，发生意外错误"))
    }

    override fun queryHeaders(): Response<List<ColumnDSL>> {
        return Response.success(tableTemplateService.queryHeaders(TemplateType.ROOM_LIST_TEMPLATE))
    }
    override fun download(): XSSFWorkbook {
        val headers = queryHeaders().data ?: run{
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

    override fun findByKeyword(keyword: String): Response<List<RoomDTO>> {
        TODO("Not yet implemented")
    }

    override fun upload(file: MultipartFile): Response<Unit> {
        val excelSheetDefinitions = getExcelSheetDefinitions()
        val rooms = file.read(excelSheetDefinitions, RoomUploadConverter::convert)
        roomRepository.saveAll(rooms)
        return Response.success("上传成功")
    }
}