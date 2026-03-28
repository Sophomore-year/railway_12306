package org.zys.railway_12306.service.order.service;

import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderItemQueryReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderPassengerDetailRespDTO;

import java.util.List;

/**
 *订单明细接口层
 *
 * @author SUM
 * @date 2026/03/28
 */
public interface OrderItemService {

    /**
     * 根据子订单记录id查询车票子订单详情
     *
     * @param requestParam 请求参数
     */
    List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam);
}
