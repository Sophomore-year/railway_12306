package org.zys.railway_12306.service.pay.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.railway_12306.service.pay.pojo.dto.base.AbstractRefundRequest;

import java.math.BigDecimal;

/**
 *退款请求命令
 *
 * @author SUM
 * @date 2026/03/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public final class RefundCommand extends AbstractRefundRequest {

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 交易凭证号
     */
    private String tradeNo;
}
