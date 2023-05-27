package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.common.enums.GoodsSortField
import com.ryouonritsu.ic.domain.dto.GoodsDTO
import com.ryouonritsu.ic.domain.protocol.request.ModifyGoodsRequest
import com.ryouonritsu.ic.domain.protocol.response.ListGoodsResponse
import com.ryouonritsu.ic.domain.protocol.response.Response
import com.ryouonritsu.ic.entity.Goods
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal

/**
 * @author ryouonritsu
 */
interface GoodsService {
    fun downloadTemplate(): XSSFWorkbook
    fun upload(file: MultipartFile): Response<Unit>
    fun findById(id: Long): Response<GoodsDTO>
    fun list(
        keyword: String?,
        type: String?,
        state: Goods.State?,
        priceFloor: BigDecimal?,
        priceCeil: BigDecimal?,
        sortField: GoodsSortField,
        page: Int,
        limit: Int
    ): Response<ListGoodsResponse>

    fun download(
        keyword: String?,
        type: String?,
        state: Goods.State?,
        priceFloor: BigDecimal?,
        priceCeil: BigDecimal?,
        sortField: GoodsSortField
    ): Response<Unit>

    fun adminModifyState(request: ModifyGoodsRequest): Response<Unit>
    fun modify(request: ModifyGoodsRequest): Response<Unit>
    fun delete(id: Long): Response<Unit>
}