package org.zys.railway_12306.service.ticket.pojo.dto.resp;

import lombok.Data;

import java.util.Date;

/**
 *车票退款返回详情实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
public class RefundTicketRespDTO {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 支付流水号
     */
    private String paySn;

    /**
     * 退款金额（单位：分）
     */
    private Integer refundAmount;

    /**
     * 退款状态
     */
    private Integer refundStatus;

    /**
     * 退款时间
     */
    private Date refundTime;
}
