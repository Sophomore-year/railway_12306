package org.zys.railway_12306.serivce.ticket.pojo.dto.req;

import lombok.Data;

import java.util.List;

/**
 *车票子订单查询
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
public class TicketOrderItemQueryReqDTO {
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 子订单记录id
     */
    private List<String> orderItemRecordIds;
}
