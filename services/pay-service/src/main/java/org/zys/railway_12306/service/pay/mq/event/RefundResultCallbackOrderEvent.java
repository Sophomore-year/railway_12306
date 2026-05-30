package org.zys.railway_12306.service.pay.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zys.railway_12306.service.pay.enums.RefundTypeEnum;
import org.zys.railway_12306.service.pay.remote.dto.TicketOrderPassengerDetailRespDTO;

import java.util.List;

/**
 * 退款结果回调订单服务事件
 *
 * @author SUM
 * @date 2026/05/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class RefundResultCallbackOrderEvent {
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 退款类型
     */
    private RefundTypeEnum refundTypeEnum;

    /**
     * 部分退款车票详情
     */
    private List<TicketOrderPassengerDetailRespDTO> partialRefundTicketDetailList;
}
