package com.ryouonritsu.ic.component.file.converter

import com.ryouonritsu.ic.common.constants.ICConstant.LONG_0
import com.ryouonritsu.ic.component.ColumnDSL
import com.ryouonritsu.ic.component.load
import com.ryouonritsu.ic.entity.Room
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row


/**
 * @author PaulManstein
 */
object RoomUploadConverter {
    fun convert(row: Row, evaluator: FormulaEvaluator, columnDefinitions: List<ColumnDSL>): Room {
        val room = Room(
            userid = LONG_0,
            status = LONG_0
        )
        return room.load(row, evaluator, columnDefinitions)
    }
}