package com.ryouonritsu.ic.service.impl

import com.ryouonritsu.ic.common.constants.ICConstant.INT_1
import com.ryouonritsu.ic.common.constants.ICConstant.INT_40000
import com.ryouonritsu.ic.common.constants.ICConstant.LONG_0
import com.ryouonritsu.ic.common.constants.TemplateType
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.enums.GoodsSortField
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.component.file.converter.GoodsUploadConverter
import com.ryouonritsu.ic.component.getTemplate
import com.ryouonritsu.ic.component.process
import com.ryouonritsu.ic.component.read
import com.ryouonritsu.ic.domain.dto.GoodsDTO
import com.ryouonritsu.ic.domain.protocol.request.ModifyGoodsRequest
import com.ryouonritsu.ic.domain.protocol.response.ListGoodsResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.Goods
import com.ryouonritsu.ic.repository.GoodsRepository
import com.ryouonritsu.ic.repository.UserRepository
import com.ryouonritsu.ic.service.GoodsService
import com.ryouonritsu.ic.service.TableTemplateService
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.math.BigDecimal
import javax.persistence.criteria.Predicate
import kotlin.jvm.optionals.getOrElse

/**
 * @author ryouonritsu
 */
@Service
class GoodsServiceImpl(
    private val userServiceImpl: UserServiceImpl,
    private val userRepository: UserRepository,
    private val goodsRepository: GoodsRepository,
    private val tableTemplateService: TableTemplateService,
    private val transactionTemplate: TransactionTemplate,
    private val asyncTaskExecutor: ThreadPoolTaskExecutor,
    @Value("\${static.file.prefix}")
    private val staticFilePrefix: String
) : GoodsService {
    companion object {
        private val log = LoggerFactory.getLogger(GoodsServiceImpl::class.java)
    }

    override fun downloadTemplate(): XSSFWorkbook {
        val excelSheetDefinitions = tableTemplateService
            .queryExcelSheetDefinitions(TemplateType.GOODS_UPLOAD_TEMPLATE)
        val goods = Goods(
            name = "Mac Book Pro",
            type = "电脑",
            amount = 100,
            price = "20000".toBigDecimal(),
            discount = "0.99".toBigDecimal(),
            description = "每一个程序员必备的工具!"
        )
        return XSSFWorkbook().getTemplate(excelSheetDefinitions, listOf(goods))
    }

    override fun upload(file: MultipartFile): Response<Unit> {
        val excelSheetDefinitions = tableTemplateService
            .queryExcelSheetDefinitions(TemplateType.GOODS_UPLOAD_TEMPLATE)
        val goods = file.read(excelSheetDefinitions, GoodsUploadConverter::convert)
        transactionTemplate.execute { goodsRepository.saveAll(goods) }
        return Response.success("上传成功")
    }

    override fun findById(id: Long): Response<GoodsDTO> {
        val goods = goodsRepository.findById(id).getOrElse {
            log.warn("[findById] cannot find goods by id = $id")
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        asyncAdjustViewCnt(goods, 1L)
        return Response.success(goods.toDTO())
    }

    internal fun asyncAdjustViewCnt(goods: Goods, value: Long) {
        asyncTaskExecutor.execute {
            goods.viewCnt += value
            if (goods.viewCnt < LONG_0) goods.viewCnt = LONG_0
            transactionTemplate.execute { goodsRepository.save(goods) }
        }
    }

    override fun list(
        keyword: String?,
        type: String?,
        state: Goods.State?,
        priceFloor: BigDecimal?,
        priceCeil: BigDecimal?,
        sortField: GoodsSortField,
        page: Int,
        limit: Int
    ): Response<ListGoodsResponse> {
        val specification = Specification<Goods> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()
            if (!keyword.isNullOrBlank())
                predicates += cb.or(
                    cb.like(root["name"], "%$keyword%"),
                    cb.like(root["type"], "%$keyword%"),
                    cb.like(root["description"], "%$keyword%")
                )
            if (!type.isNullOrBlank())
                predicates += cb.like(root["type"], "%$type%")
            if (state != null)
                predicates += cb.equal(root.get<Int>("state"), state.code)
            if (priceFloor != null)
                predicates += cb.ge(
                    cb.prod(root["price"], root["discount"]),
                    priceFloor
                )
            if (priceCeil != null)
                predicates += cb.le(
                    cb.prod(root["price"], root["discount"]),
                    priceCeil
                )
            predicates += cb.equal(root.get<Boolean>("status"), true)
            query.where(*predicates.toTypedArray())
                .orderBy(
                    when (sortField) {
                        GoodsSortField.VIEW_CNT_DESC -> cb.desc(root.get<Long>("viewCnt"))
                        GoodsSortField.VIEW_CNT_ASC -> cb.asc(root.get<Long>("viewCnt"))
                        GoodsSortField.PRICE_ASC -> cb.asc(cb.prod(root["price"], root["discount"]))
                        GoodsSortField.PRICE_DESC -> cb.desc(
                            cb.prod(root["price"], root["discount"])
                        )

                        GoodsSortField.SALES_DESC -> cb.desc(root.get<Long>("sales"))
                        GoodsSortField.SALES_ASC -> cb.asc(root.get<Long>("sales"))
                    }
                ).restriction
        }
        val result = goodsRepository.findAll(specification, PageRequest.of(page - 1, limit))
        val total = result.totalElements
        val list = result.content.map { it.toDTO() }
        return Response.success(ListGoodsResponse(list, total))
    }

    override fun download(
        keyword: String?,
        type: String?,
        state: Goods.State?,
        priceFloor: BigDecimal?,
        priceCeil: BigDecimal?,
        sortField: GoodsSortField
    ): Response<Unit> {
        val headers = tableTemplateService.queryHeaders(TemplateType.GOODS_LIST_TEMPLATE)
        val data = list(keyword, type, state, priceFloor, priceCeil, sortField, INT_1, INT_40000)
            .data?.list ?: listOf()
        asyncDownload(headers, data)
        return Response.success("下载任务已提交, 若下载成功会将下载链接以邮件形式发送到您的邮箱")
    }

    override fun adminModifyState(request: ModifyGoodsRequest): Response<Unit> {
        val goods = goodsRepository.findById(request.id!!).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        if (request.state != null) goods.state = request.state.code
        transactionTemplate.execute { goodsRepository.save(goods) }
        return Response.success()
    }

    override fun modify(request: ModifyGoodsRequest): Response<Unit> {
        val goods = goodsRepository.findById(request.id!!).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        if (!request.name.isNullOrBlank()) goods.name = request.name
        if (!request.picture.isNullOrBlank()) goods.picture = request.picture
        if (!request.type.isNullOrBlank()) goods.type = request.type
        if (request.amount != null) goods.amount = request.amount
        if (goods.state != Goods.State.UNDER_REVIEW.code && request.state != null)
            goods.state = request.state.code
        if (request.price != null) goods.price = request.price
        if (request.discount != null) goods.discount = request.discount
        if (!request.description.isNullOrBlank()) goods.description = request.description

        transactionTemplate.execute { goodsRepository.save(goods) }
        return Response.success()
    }

    override fun delete(id: Long): Response<Unit> {
        val goods = goodsRepository.findById(id).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        goods.status = false
        transactionTemplate.execute { goodsRepository.save(goods) }
        return Response.success()
    }

    private fun asyncDownload(headers: List<ColumnDSL>, data: List<GoodsDTO>) {
        val time = System.currentTimeMillis()
        val userId = RequestContext.userId.get()
        val fileDir = "static/file/${userId!!}"
        val fileName = "goods_${time}.xlsx"
        val filePath = "$fileDir/$fileName"
        val f = asyncTaskExecutor.submitListenable {
            val wb = XSSFWorkbook().process(headers, data, "Goods")
            val dir = File(fileDir)
            if (!dir.exists()) dir.mkdirs()
            wb.write(File(filePath).outputStream())
        }
        f.addCallback(
            {
                log.info("[asyncDownload] async download successfully! filePath = $filePath")
                userServiceImpl.retrySendEmail(
                    userRepository.findById(userId).get().email,
                    "$fileName download completed",
                    "http://$staticFilePrefix:8090/file/${userId}/${fileName}"
                )
            },
            { log.error("[asyncDownload] async download failed", it) }
        )
    }
}