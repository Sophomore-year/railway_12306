package org.zys.railway_12306.service.pay.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.railway_12306.service.pay.pojo.dto.base.AbstractPayRequest;

import java.math.BigDecimal;

/**
 *支付请求命令
 *
 * @author SUM
 * @date 2026/03/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public final class PayCommand extends AbstractPayRequest {

    /**
     * 子订单号
     */
    private String outOrderSn;

    /**
     * 订单总金额
     * 单位为元，精确到小数点后两位，取值范围：[0.01,100000000]
     */
    private BigDecimal totalAmount;

    /**
     * 订单标题
     * 注意：不可使用特殊字符，如 /，=，& 等
     */
    private String subject;
}
