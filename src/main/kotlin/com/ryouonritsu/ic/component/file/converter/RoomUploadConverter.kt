package com.ryouonritsu.ic.component.file.converter

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
            userId = null,
            status = false,
            commence = null,
            terminate = null,
            contract = null
        )
        return room.load(row, evaluator, columnDefinitions)
    }
}