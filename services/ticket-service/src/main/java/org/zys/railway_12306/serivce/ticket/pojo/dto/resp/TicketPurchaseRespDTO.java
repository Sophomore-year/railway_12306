package org.zys.railway_12306.serivce.ticket.pojo.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *车票购买返回参数
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketPurchaseRespDTO {
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 乘车人订单详情
     */
    private List<TicketOrderDetailRespDTO> ticketOrderDetails;
}
