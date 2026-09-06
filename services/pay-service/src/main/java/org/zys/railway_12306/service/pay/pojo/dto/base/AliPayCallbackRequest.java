package org.zys.railway_12306.service.pay.pojo.dto.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.railway_12306.service.pay.enums.PayChannelEnum;

import java.util.Date;
import java.util.Map;

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

    /**
     * 回调原始参数（含 sign），用于 RSA2 异步通知验签，不落库
     */
    private Map<String, String> originParams;

    @Override
    public AliPayCallbackRequest getAliPayCallBackRequest() {
        return this;
    }

    @Override
    public String buildMark() {
        // 回调策略标识固定为 ALI_PAY_CALLBACK，与支付(ALI_PAY)、退款(ALI_PAY_REFUND)策略区分
        return "ALI_PAY_CALLBACK";
    }
}
