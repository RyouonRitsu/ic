package com.ryouonritsu.ic.service

import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.component.file.ExcelSheetDefinition

/**
 * @author ryouonritsu
 */
interface TableTemplateService {
    fun queryHeaders(headCode: Int): List<ColumnDSL>
    fun queryExcelSheetDefinitions(templateCode: Int): List<ExcelSheetDefinition>
}