package org.zys.railway_12306.service.order.service;

import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderDetailRespDTO;

public interface OrderService {

    /**
     * 根据订单号查询车票订单
     *
     * @param orderSn 订单号
     * @return {@link TicketOrderDetailRespDTO}
     */
    TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn);
}
