package com.ryouonritsu.ic.controller

import com.ryouonritsu.ic.common.annotation.AuthCheck
import com.ryouonritsu.ic.common.annotation.ServiceLog
import com.ryouonritsu.ic.domain.protocol.request.*
import com.ryouonritsu.ic.entity.Order
import com.ryouonritsu.ic.service.ShoppingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * @author ryouonritsu
 */
@Validated
@RestController
@RequestMapping("/shopping")
@Tag(name = "购物接口")
class ShoppingController(
    private val shoppingService: ShoppingService
) {
    companion object {
        private val log = LoggerFactory.getLogger(ShoppingController::class.java)
    }

    @ServiceLog(description = "查询购物车")
    @GetMapping("/listCart")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "查询购物车", description = "查询购物车")
    fun listCart(
        @RequestParam @Parameter(description = "页数, 从1开始", required = true)
        @Valid @NotNull @Min(1) page: Int?,
        @RequestParam @Parameter(description = "每页数量", required = true)
        @Valid @NotNull @Min(1) limit: Int?
    ) = shoppingService.listCart(page!!, limit!!)

    @ServiceLog(description = "添加到购物车")
    @PostMapping("/addToCart")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "添加到购物车", description = "添加到购物车")
    fun addToCart(@RequestBody @Valid request: AddToCartRequest) =
        shoppingService.addToCart(request.goodsId!!, request.amount!!)

    @ServiceLog(description = "编辑购物车")
    @PostMapping("/modifyAmount")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "编辑购物车", description = "编辑购物车")
    fun modifyAmount(@RequestBody @Valid request: ModifyAmountRequest) =
        shoppingService.modifyAmount(request.recordId!!, request.value!!)

    @ServiceLog(description = "从购物车删除")
    @PostMapping("/removeFromCart")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "从购物车删除", description = "从购物车删除")
    fun removeFromCart(@RequestBody @Valid request: RemoveFromCartRequest) =
        shoppingService.removeFromCart(request.recordIds!!)

    @ServiceLog(description = "订单查询")
    @GetMapping("/listOrders")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "订单查询", description = "订单查询")
    fun listOrders(
        @RequestParam(required = false) @Parameter(description = "关键词") keyword: String?,
        @RequestParam(required = false) @Parameter(description = "状态集合") states: List<Order.State>?,
        @RequestParam @Parameter(description = "页数, 从1开始", required = true)
        @Valid @NotNull @Min(1) page: Int?,
        @RequestParam @Parameter(description = "每页数量", required = true)
        @Valid @NotNull @Min(1) limit: Int?
    ) = shoppingService.listOrders(keyword, states, page!!, limit!!)

    @ServiceLog(description = "批量下单")
    @PostMapping("/bulkOrder")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "批量下单", description = "批量下单")
    fun bulkOrder(@RequestBody @Valid request: BulkOrderRequest) =
        shoppingService.bulkOrder(request.recordIds!!, request.address!!)

    @ServiceLog(description = "下单")
    @PostMapping("/order")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "下单", description = "下单")
    fun order(@RequestBody @Valid request: OrderRequest) =
        shoppingService.order(request.goodsId!!, request.amount!!, request.address!!)

    @ServiceLog(description = "支付")
    @PostMapping("/pay")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "支付", description = "支付")
    fun pay(@RequestBody @Valid request: PayRequest) =
        shoppingService.pay(request.orderId!!)

    @ServiceLog(description = "充值")
    @PostMapping("/recharge")
    @AuthCheck
    @Tag(name = "购物接口")
    @Operation(summary = "充值", description = "充值")
    fun recharge(@RequestBody @Valid request: RechargeRequest) =
        shoppingService.recharge(request.value!!)
}