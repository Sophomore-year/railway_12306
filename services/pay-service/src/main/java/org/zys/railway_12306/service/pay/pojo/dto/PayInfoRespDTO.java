package org.zys.railway_12306.service.pay.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 *支付单详情信息返回参数
 *
 * @author SUM
 * @date 2026/03/29
 */
@Data
public class PayInfoRespDTO {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 支付总金额
     */
    private Integer totalAmount;

    /**
     * 支付状态
     */
    private Integer status;

    /**
     * 支付时间
     */
    private Date gmtPayment;
}
