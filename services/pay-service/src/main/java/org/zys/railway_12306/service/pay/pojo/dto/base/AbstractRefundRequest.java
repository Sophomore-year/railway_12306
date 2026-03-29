package org.zys.railway_12306.service.pay.pojo.dto.base;

import lombok.Getter;
import lombok.Setter;

/**
 *抽象退款入参实体
 *
 * @author SUM
 * @date 2026/03/29
 */
public abstract class AbstractRefundRequest implements RefundRequest{

    /**
     * 交易环境，H5、小程序、网站等
     */
    @Getter
    @Setter
    private Integer tradeType;

    /**
     * 订单号
     */
    @Getter
    @Setter
    private String orderSn;

    /**
     * 支付渠道
     */
    @Getter
    @Setter
    private Integer channel;

    @Override
    public AliRefundRequest getAliRefundRequest() {
        return null;
    }

    @Override
    public String buildMark() {
        return null;
    }
}
