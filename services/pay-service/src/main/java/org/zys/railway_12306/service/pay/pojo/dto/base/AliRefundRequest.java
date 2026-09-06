package org.zys.railway_12306.service.pay.pojo.dto.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 *支付宝退款请求入参
 *
 * @author SUM
 * @date 2026/03/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public final class AliRefundRequest extends AbstractRefundRequest {

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 交易凭证号
     */
    private String tradeNo;

    @Override
    public AliRefundRequest getAliRefundRequest() {
        return this;
    }

    @Override
    public String buildMark() {
        // 退款策略标识固定为 ALI_PAY_REFUND，与支付(ALI_PAY)、回调(ALI_PAY_CALLBACK)策略区分
        return "ALI_PAY_REFUND";
    }
}
