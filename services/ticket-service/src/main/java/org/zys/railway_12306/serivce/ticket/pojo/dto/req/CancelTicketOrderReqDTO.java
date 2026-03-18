package org.zys.railway_12306.serivce.ticket.pojo.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *取消车票订单请求入参
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelTicketOrderReqDTO {
    /**
     * 订单号
     */
    private String orderSn;
}
