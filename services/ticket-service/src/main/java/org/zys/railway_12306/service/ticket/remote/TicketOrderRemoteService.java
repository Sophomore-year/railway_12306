package org.zys.railway_12306.service.ticket.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.service.ticket.pojo.dto.req.CancelTicketOrderReqDTO;
import org.zys.railway_12306.service.ticket.remote.dto.TicketOrderCreateRemoteReqDTO;
import org.zys.railway_12306.service.ticket.remote.dto.TicketOrderDetailRespDTO;

/**
 *车票订单远程服务调用
 *
 * @author SUM
 * @date 2026/03/25
 */
@FeignClient(value = "railway12306-order${unique-name:}-service", url = "${aggregation.remote-url:}")
public interface TicketOrderRemoteService {
    /**
     * 创建车票订单
     *
     * @param requestParam 创建车票订单请求参数
     * @return 创建车票订单结果
     */
    @PostMapping("/api/order-service/order/ticket/create")
    Result<String> createTicketOrder(@RequestBody TicketOrderCreateRemoteReqDTO requestParam);

    /**
     * 车票订单取消
     *
     * @param requestParam 车票订单取消入参
     * @return 订单取消返回结果
     */
    @PostMapping("/api/order-service/order/ticket/cancel")
    Result<Void> cancelTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam);


    /**
     * 跟据订单号查询车票订单
     *
     * @param orderSn 列车订单号
     * @return 列车订单记录
     */
    @GetMapping("/api/order-service/order/ticket/query")
    Result<TicketOrderDetailRespDTO> queryTicketOrderByOrderSn(@RequestParam(value = "orderSn") String orderSn);

}
