package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.common.enums.GoodsSortField
import com.ryouonritsu.ic.common.utils.DownloadUtils
import com.ryouonritsu.ic.domain.protocol.request.DeleteRequest
import com.ryouonritsu.ic.domain.protocol.request.ModifyGoodsRequest
import com.ryouonritsu.ic.domain.protocol.request.UserUploadRequest
import com.ryouonritsu.ic.service.GoodsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Validated
@RestController
@RequestMapping("/goods")
@Tag(name = "商品接口")
class GoodsController(
    private val goodsService: GoodsService
) {
    companion object {
        private val log = LoggerFactory.getLogger(GoodsController::class.java)
    }

    @ServiceLog(description = "商品上传模板下载", printResponse = false)
    @GetMapping("/downloadTemplate")
    @AuthCheck
    @Tag(name = "商品接口")
    @Operation(summary = "商品上传模板下载", description = "商品上传模板下载")
    fun downloadTemplate(): ResponseEntity<ByteArray> {
        try {
            goodsService.downloadTemplate().use {
                ByteArrayOutputStream().use { os ->
                    it.write(os)
                    return DownloadUtils.downloadFile("goods_template.xlsx", os.toByteArray())
                }
            }
        } catch (e: Exception) {
            log.error("[downloadTemplate] failed to download goods template", e)
            throw e
        }
    }

    @ServiceLog(description = "商品上传", printRequest = false)
    @PostMapping("/upload")
    @AuthCheck
    @Tag(name = "商品接口")
    @Operation(summary = "商品上传", description = "商品上传")
    fun upload(@RequestBody @Valid request: UserUploadRequest) = goodsService.upload(request.file!!)

    @ServiceLog(description = "根据Id查询商品")
    @GetMapping("/findById")
    @Tag(name = "商品接口")
    @Operation(summary = "根据Id查询商品", description = "根据Id查询商品")
    fun findById(
        @RequestParam @Parameter(description = "商品ID", required = true)
        @Valid @NotNull id: Long?
    ) = goodsService.findById(id!!)

    @ServiceLog(description = "商品查询")
    @GetMapping("/list")
    @Tag(name = "商品接口")
    @Operation(summary = "商品查询", description = "商品查询")
    fun list(
        @RequestParam(required = false) @Parameter(description = "关键词") keyword: String?,
        @RequestParam(required = false) @Parameter(description = "类型") type: String?,
        @RequestParam(required = false) @Parameter(description = "状态") state: Int?,
        @RequestParam(required = false) @Parameter(description = "价格下限") priceFloor: BigDecimal?,
        @RequestParam(required = false) @Parameter(description = "价格上限") priceCeil: BigDecimal?,
        @RequestParam(required = false) @Parameter(description = "排序字段")
        sortField: GoodsSortField = GoodsSortField.VIEW_CNT_DESC,
        @RequestParam @Parameter(description = "页数, 从1开始", required = true)
        @Valid @NotNull @Min(1) page: Int = 1,
        @RequestParam @Parameter(description = "每页数量", required = true)
        @Valid @NotNull @Min(1) limit: Int = 10
    ) = goodsService.list(keyword, type, state, priceFloor, priceCeil, sortField, page, limit)

    @ServiceLog(description = "商品查询结果下载")
    @GetMapping("/download")
    @AuthCheck
    @Tag(name = "商品接口")
    @Operation(summary = "商品查询结果下载", description = "商品查询结果下载")
    fun download(
        @RequestParam(required = false) @Parameter(description = "关键词") keyword: String?,
        @RequestParam(required = false) @Parameter(description = "类型") type: String?,
        @RequestParam(required = false) @Parameter(description = "状态") state: Int?,
        @RequestParam(required = false) @Parameter(description = "价格下限") priceFloor: BigDecimal?,
        @RequestParam(required = false) @Parameter(description = "价格上限") priceCeil: BigDecimal?,
        @RequestParam(required = false) @Parameter(description = "排序字段")
        sortField: GoodsSortField = GoodsSortField.VIEW_CNT_DESC,
    ) = goodsService.download(keyword, type, state, priceFloor, priceCeil, sortField)

    @ServiceLog(description = "商品编辑")
    @PostMapping("/modify")
    @AuthCheck
    @Tag(name = "商品接口")
    @Operation(summary = "商品编辑", description = "商品编辑")
    fun modify(@RequestBody @Valid request: ModifyGoodsRequest) = goodsService.modify(request)

    @ServiceLog(description = "商品删除")
    @PostMapping("/delete")
    @AuthCheck
    @Tag(name = "商品接口")
    @Operation(summary = "商品删除", description = "商品删除")
    fun delete(@RequestBody @Valid request: DeleteRequest) = goodsService.delete(request.id!!)
}