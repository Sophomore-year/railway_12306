package org.zys.railway_12306.service.pay.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 *退款详情返回参数实体
 *
 * @author SUM
 * @date 2026/03/29
 */
@Data
public class RefundRespDTO {

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
     * 退款状态，对应 {@link org.zys.railway_12306.service.pay.enums.TradeStatusEnum#tradeCode()}
     */
    private Integer status;

    /**
     * 退款时间
     */
    private Date refundTime;
}
