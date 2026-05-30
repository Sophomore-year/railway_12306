package org.zys.railway_12306.service.pay.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.service.pay.remote.dto.TicketOrderDetailRespDTO;

/**
 *车票订单远程服务调用
 *
 * @author SUM
 * @date 2026/05/30
 */
@FeignClient(value = "railway12306-order${unique-name:}-service")
public interface TicketOrderRemoteService {
    /**
     * 跟据订单号查询车票订单
     *
     * @param orderSn 列车订单号
     * @return 列车订单记录
     */
    @GetMapping("/api/order-service/order/ticket/query")
    Result<TicketOrderDetailRespDTO> queryTicketOrderByOrderSn(@RequestParam(value = "orderSn") String orderSn);
}
