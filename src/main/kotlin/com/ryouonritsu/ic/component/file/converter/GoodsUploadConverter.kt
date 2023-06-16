package com.ryouonritsu.ic.component.file.converter

import com.ryouonritsu.ic.common.utils.RequestContext
import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.entity.Goods
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row

/**
 * @author ryouonritsu
 */
object GoodsUploadConverter {
    private val formatter by lazy { DataFormatter() }

    fun convert(row: Row, columnDefinitions: List<ColumnDSL>): Goods {
        val evaluator = row.sheet.workbook.creationHelper.createFormulaEvaluator()

        fun getValue(index: Int): String {
            return formatter.formatCellValue(
                row.getCell(index).apply { cellStyle = null },
                evaluator
            )
        }

        return Goods(
            name = getValue(0),
            type = getValue(1),
            amount = getValue(2).toLong(),
            state = Goods.State.UNDER_REVIEW.code,
            price = getValue(3).toBigDecimal(),
            discount = getValue(4).toBigDecimal(),
            description = getValue(5),
            userId = RequestContext.user!!.id
        )
    }
}