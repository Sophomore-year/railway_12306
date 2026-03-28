package org.zys.railway_12306.service.order.pojo.dto.req;

import lombok.Data;

/**
 *取消车票订单请求入参
 *
 * @author SUM
 * @date 2026/03/28
 */
@Data
public class CancelTicketOrderReqDTO {

    /**
     * 订单号
     */
    private String orderSn;
}
