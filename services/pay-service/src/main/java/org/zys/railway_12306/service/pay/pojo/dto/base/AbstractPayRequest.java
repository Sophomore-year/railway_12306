package org.zys.railway_12306.service.pay.pojo.dto.base;

import lombok.Getter;
import lombok.Setter;
import org.zys.rail_12306.framework.starter.distributedid.toolkit.SnowflakeIdUtil;

/**
 *抽象支付入参实体
 *
 * @author SUM
 * @date 2026/03/29
 */
public abstract class AbstractPayRequest implements PayRequest{

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

    /**
     * 商户订单号
     * 由商家自定义，64个字符以内，仅支持字母、数字、下划线且需保证在商户端不重复
     */
    @Getter
    @Setter
    private String orderRequestId = SnowflakeIdUtil.nextIdStr();

    @Override
    public AliPayRequest getAliPayRequest() {
        return null;
    }

    @Override
    public String getOrderRequestId() {
        return orderRequestId;
    }

    @Override
    public String buildMark() {
        return null;
    }
}
