package com.ryouonritsu.ic.component.file.converter

import com.ryouonritsu.ic.common.constants.ICConstant.EMPTY_STR
import com.ryouonritsu.ic.common.utils.MD5Util
import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.component.load
import com.ryouonritsu.ic.entity.User
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row

/**
 * @author ryouonritsu
 */
object UserUploadConverter {
    fun convert(row: Row, evaluator: FormulaEvaluator, columnDefinitions: List<ColumnDSL>): User {
        val user = User(
            email = EMPTY_STR,
            username = EMPTY_STR,
            password = EMPTY_STR
        )
        return user.load(row, evaluator, columnDefinitions).apply {
            password = MD5Util.encode(username)
        }
    }
}