package com.ryouonritsu.ic.component

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.JSONPath
import com.alibaba.fastjson2.to
import com.ryouonritsu.ic.common.constants.ICConstant.DEFAULT_DELIMITER
import com.ryouonritsu.ic.common.constants.ICConstant.EMPTY_STR
import com.ryouonritsu.ic.common.constants.ICConstant.INT_0
import com.ryouonritsu.ic.common.constants.ICConstant.INT_184
import com.ryouonritsu.ic.common.constants.ICConstant.INT_256
import com.ryouonritsu.ic.common.constants.ICConstant.INT_65280
import com.ryouonritsu.ic.common.constants.ICConstant.UNIQUE
import com.ryouonritsu.ic.common.enums.DataTypeEnum
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.component.file.ExcelSheetDefinition
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import kotlin.math.max
import kotlin.math.min

private val formatter by lazy { DataFormatter() }
private val log: Logger = LoggerFactory.getLogger("TemplateProcessor")

/**
 * @author ryouonritsu
 */
fun <T> XSSFWorkbook.process(
    columnDefinitions: List<ColumnDSL>,
    data: List<T>,
    sheetName: String? = null
): XSSFWorkbook {
    val sheet = if (sheetName != null) this.createSheet(sheetName) else this.createSheet()
    val maxSizeMapping = mutableMapOf<Int, Int>()

    val headerRow = sheet.createRow(0)
    columnDefinitions.forEachIndexed { index, columnDSL ->
        val cell = headerRow.createCell(index)
        cell.cellStyle = this.createCellStyle()
            .apply {
                setFont(this@process.createFont().apply { bold = true })
                borderTop = BorderStyle.THIN
                borderBottom = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            }
        cell.setCellValue(columnDSL.columnName)

        maxSizeMapping[index] = max(
            maxSizeMapping[index] ?: INT_0,
            columnDSL.columnName?.toByteArray()?.size ?: INT_0
        )
    }

    val styleMap = mutableMapOf<Int, XSSFCellStyle>()

    data.forEachIndexed { index, t ->
        val row = sheet.createRow(index + 1)
        columnDefinitions.forEachIndexed { id, columnDSL ->
            val fields = columnDSL.dataPaths.map { JSONPath.eval(t, it) }
            val value = if (fields.isEmpty()) EMPTY_STR
            else fields.joinToString(DEFAULT_DELIMITER)
            val cell = row.createCell(id)
            when (DataTypeEnum.getByType(columnDSL.dataType ?: run {
                log.error("[TemplateProcessor::XSSFWorkbook.process] Data type is null")
                throw ServiceException(ExceptionEnum.DATA_TYPE_IS_INVALID)
            })) {
                DataTypeEnum.NUMBER -> cell.setCellValue(value.toDouble())
                else -> cell.setCellValue(value)
            }
            maxSizeMapping[id] = max(maxSizeMapping[id] ?: INT_0, value.toByteArray().size)
            val style by lazy {
                this.createCellStyle().apply {
                    borderTop = BorderStyle.THIN
                    borderBottom = BorderStyle.THIN
                    borderLeft = BorderStyle.THIN
                    borderRight = BorderStyle.THIN
                    alignment = HorizontalAlignment.RIGHT
                    verticalAlignment = VerticalAlignment.CENTER
                }
            }
            cell.cellStyle = if (index == INT_0) {
                styleMap[id] = style
                style
            } else styleMap[index] ?: style
        }
    }

    maxSizeMapping.forEach { (k, v) ->
        sheet.setColumnWidth(k, min(v * INT_256 + INT_184, INT_65280))
    }

    return this
}

/**
 * @author ryouonritsu
 */
fun Row.parse(evaluator: FormulaEvaluator, columnDefinitions: List<ColumnDSL>): JSONObject {
    val result = JSONObject()
    columnDefinitions.forEachIndexed { id, dsl ->
        val value = formatter.formatCellValue(
            this.getCell(id).apply { cellStyle = null },
            evaluator
        ).split(DEFAULT_DELIMITER)
        dsl.dataPaths.forEachIndexed { index, path -> JSONPath.set(result, path, value[index]) }
    }
    return result
}

/**
 * @author ryouonritsu
 */
inline fun <reified T> T.load(
    row: Row,
    evaluator: FormulaEvaluator,
    columnDefinitions: List<ColumnDSL>
): T {
    return JSONObject.from(this).apply { putAll(row.parse(evaluator, columnDefinitions)) }.to()
}

/**
 * @author ryouonritsu
 */
fun <T> MultipartFile.read(
    excelSheetDefinitions: List<ExcelSheetDefinition>,
    converter: (row: Row, evaluator: FormulaEvaluator, columnDefinitions: List<ColumnDSL>) -> T
): List<T> {
    val workbook = XSSFWorkbook(this.inputStream)
    val evaluator = workbook.creationHelper.createFormulaEvaluator()

    val data = mutableListOf<T>()
    workbook.forEachIndexed { index, sheet ->
        val columnDefinitions = excelSheetDefinitions[index].columns
        sheet.forEach {
            if (it.rowNum == 0) return@forEach
            data += converter(it, evaluator, columnDefinitions)
        }

        columnDefinitions.forEach {
            if (it.extra?.getBoolean(UNIQUE) == true) {
                val fields = mutableSetOf<String>()
                data.forEach { d ->
                    val value = it.dataPaths
                        .map { path -> JSONPath.eval(d, path) }
                        .joinToString(DEFAULT_DELIMITER)
                    if (value !in fields) fields += value
                    else throw ServiceException(ExceptionEnum.DATA_ERROR)
                }
            }
        }
    }
    return data
}

/**
 * @author ryouonritsu
 */
fun <T> XSSFWorkbook.getTemplate(
    excelSheetDefinitions: List<ExcelSheetDefinition>,
    examples: List<T> = listOf()
): XSSFWorkbook {
    excelSheetDefinitions.forEach {
        this.process(it.columns, examples, it.sheetName)
    }
    return this
}