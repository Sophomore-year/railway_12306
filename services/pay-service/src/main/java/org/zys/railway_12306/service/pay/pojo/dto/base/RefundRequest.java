package org.zys.railway_12306.service.pay.pojo.dto.base;

/**
 *退款入参接口
 *
 * @author SUM
 * @date 2026/03/29
 */
public interface RefundRequest {

    /**
     * 获取阿里退款入参
     */
    AliRefundRequest getAliRefundRequest();

    /**
     * 获取订单号
     */
    String getOrderSn();

    /**
     * 构建查找支付策略实现类标识
     */
    String buildMark();
}
