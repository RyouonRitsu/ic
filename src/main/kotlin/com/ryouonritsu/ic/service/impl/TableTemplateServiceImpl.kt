package com.ryouonritsu.ic.service.impl

import com.alibaba.fastjson2.parseArray
import com.ryouonritsu.ic.common.constants.ICConstant.INT_0
import com.ryouonritsu.ic.common.constants.ICConstant.INT_1
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.component.file.ExcelSheetDefinition
import com.ryouonritsu.ic.entity.TableTemplate
import com.ryouonritsu.ic.repository.TableTemplateRepository
import com.ryouonritsu.ic.service.TableTemplateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * @author ryouonritsu
 */
@Service
class TableTemplateServiceImpl(
    private val tableTemplateRepository: TableTemplateRepository
) : TableTemplateService {
    companion object {
        private val log = LoggerFactory.getLogger(TableTemplateServiceImpl::class.java)
    }

    private fun getTemplate(templateCode: Int): List<TableTemplate> {
        val templates = tableTemplateRepository.findByTemplateType(templateCode)
        if (templates.isEmpty()) {
            log.error("[queryHeaders] can not find any template, code = $templateCode")
            throw ServiceException(ExceptionEnum.TEMPLATE_NOT_EXIST)
        }
        if (templates.size != INT_1)
            log.warn("[queryHeaders] template is not unique, code = $templateCode")
        return templates
    }

    override fun queryHeaders(headCode: Int): List<ColumnDSL> {
        return getTemplate(headCode)[INT_0].templateInfo.parseArray<ColumnDSL>()
    }

    override fun queryExcelSheetDefinitions(templateCode: Int): List<ExcelSheetDefinition> {
        return getTemplate(templateCode)[INT_0].templateInfo.parseArray<ExcelSheetDefinition>()
    }
}