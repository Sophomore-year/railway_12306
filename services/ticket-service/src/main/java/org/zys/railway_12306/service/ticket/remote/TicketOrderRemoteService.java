package org.zys.railway_12306.service.ticket.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.service.ticket.remote.dto.TicketOrderCreateRemoteReqDTO;

/**
 *车票订单远程服务调用
 *
 * @author SUM
 * @date 2026/03/25
 */
@FeignClient(value = "railway_12306-order${unique-name:}-service", url = "${aggregation.remote-url:}")
public interface TicketOrderRemoteService {
    /**
     * 创建车票订单
     *
     * @param requestParam 创建车票订单请求参数
     * @return 创建车票订单结果
     */
    @PostMapping("/api/order-service/order/ticket/create")
    Result<String> createTicketOrder(@RequestBody TicketOrderCreateRemoteReqDTO requestParam);

}
