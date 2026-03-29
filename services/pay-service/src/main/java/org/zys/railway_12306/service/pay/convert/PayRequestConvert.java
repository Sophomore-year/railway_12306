package org.zys.railway_12306.service.pay.convert;

import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.service.pay.enums.PayChannelEnum;
import org.zys.railway_12306.service.pay.pojo.dto.PayCommand;
import org.zys.railway_12306.service.pay.pojo.dto.base.AliPayRequest;
import org.zys.railway_12306.service.pay.pojo.dto.base.PayRequest;

import java.util.Objects;

/**
 *支付请求入参转换器
 *
 * @author SUM
 * @date 2026/03/29
 */
public final class PayRequestConvert {

    /**
     * {@link PayCommand} to {@link PayRequest}
     *
     * @param payCommand 支付请求参数
     * @return {@link PayRequest}
     */
    public static PayRequest command2PayRequest(PayCommand payCommand) {
        PayRequest payRequest = null;
        if (Objects.equals(payCommand.getChannel(), PayChannelEnum.ALI_PAY.getCode())) {
            payRequest = BeanUtil.convert(payCommand, AliPayRequest.class);
        }
        return payRequest;
    }
}
