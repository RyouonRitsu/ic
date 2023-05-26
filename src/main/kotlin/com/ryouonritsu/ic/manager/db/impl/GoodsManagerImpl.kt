package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.common.constants.ICConstant.LONG_0
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.manager.db.GoodsManager
import com.ryouonritsu.ic.repository.GoodsRepository
import org.springframework.transaction.support.TransactionTemplate
import kotlin.jvm.optionals.getOrElse

/**
 * @author ryouonritsu
 */
class GoodsManagerImpl(
    private val goodsRepository: GoodsRepository,
    private val transactionTemplate: TransactionTemplate
) : GoodsManager {
    override fun adjustAmount(goodsId: Long, value: Long) {
        val goods = goodsRepository.findById(goodsId).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        goods.amount += value
        if (goods.amount < LONG_0) throw ServiceException(ExceptionEnum.DATA_ERROR)
        transactionTemplate.execute { goodsRepository.save(goods) }
    }
}