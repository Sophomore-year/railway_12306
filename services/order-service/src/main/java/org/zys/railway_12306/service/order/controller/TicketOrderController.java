package org.zys.railway_12306.service.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.web.Results;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderDetailRespDTO;
import org.zys.railway_12306.service.order.service.OrderService;

/**
 *车票订单接口控制层
 *
 * @author SUM
 * @date 2026/03/28
 */
@RestController
@RequiredArgsConstructor
public class TicketOrderController {

    private final OrderService orderService;

    /**
     * 根据订单号查询车票订单
     */
    @GetMapping("/api/order-service/order/ticket/query")
    public Result<TicketOrderDetailRespDTO> queryTicketOrderByOrderSn(@RequestParam(value = "orderSn") String orderSn) {
        return Results.success(orderService.queryTicketOrderByOrderSn(orderSn));
    }

}
