package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.common.constants.ICConstant.LONG_0
import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.entity.Goods
import com.ryouonritsu.ic.manager.db.GoodsManager
import com.ryouonritsu.ic.repository.GoodsRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import kotlin.jvm.optionals.getOrElse

/**
 * @author ryouonritsu
 */
@Component
class GoodsManagerImpl(
    private val goodsRepository: GoodsRepository,
    private val transactionTemplate: TransactionTemplate
) : GoodsManager {
    override fun adjustProperties(goodsId: Long, amountValue: Long, salesValue: Long) {
        val goods = goodsRepository.findById(goodsId).getOrElse {
            throw ServiceException(ExceptionEnum.NOT_FOUND)
        }
        adjustProperties(goods, amountValue, salesValue)
    }

    override fun adjustProperties(goods: Goods, amountValue: Long, salesValue: Long) {
        goods.amount += amountValue
        goods.sales += salesValue
        if (goods.amount < LONG_0 || goods.sales < LONG_0)
            throw ServiceException(ExceptionEnum.INSUFFICIENT_AMOUNT)
        transactionTemplate.execute { goodsRepository.save(goods) }
    }
}