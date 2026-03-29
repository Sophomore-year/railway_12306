package org.zys.railway_12306.service.pay.pojo.dto.base;

/**
 *支付回调请求入参
 *
 * @author SUM
 * @date 2026/03/29
 */
public interface PayCallbackRequest {

    /**
     * 获取阿里支付回调入参
     */
    AliPayCallbackRequest getAliPayCallBackRequest();

    /**
     * 构建查找支付回调策略实现类标识
     */
    String buildMark();
}
