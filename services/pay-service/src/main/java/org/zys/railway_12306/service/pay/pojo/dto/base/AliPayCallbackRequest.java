package org.zys.railway_12306.service.pay.pojo.dto.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.railway_12306.service.pay.enums.PayChannelEnum;

import java.util.Date;

/**
 *支付宝回调请求入参
 *
 * @author SUM
 * @date 2026/03/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public final class AliPayCallbackRequest extends AbstractPayCallbackRequest{

    /**
     * 支付渠道
     */
    private String channel;

    /**
     * 支付状态
     */
    private String tradeStatus;

    /**
     * 支付凭证号
     */
    private String tradeNo;

    /**
     * 买家付款时间
     */
    private Date gmtPayment;

    /**
     * 买家付款金额
     */
    private Integer buyerPayAmount;

    @Override
    public AliPayCallbackRequest getAliPayCallBackRequest() {
        return this;
    }

    @Override
    public String buildMark() {
        return PayChannelEnum.ALI_PAY.getName();
    }
}
