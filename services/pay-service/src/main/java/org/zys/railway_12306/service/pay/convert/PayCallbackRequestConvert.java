package org.zys.railway_12306.service.pay.convert;

import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.service.pay.enums.PayChannelEnum;
import org.zys.railway_12306.service.pay.pojo.dto.PayCallbackCommand;
import org.zys.railway_12306.service.pay.pojo.dto.base.AliPayCallbackRequest;
import org.zys.railway_12306.service.pay.pojo.dto.base.PayCallbackRequest;

import java.util.Objects;

/**
 *支付回调请求入参转换器
 *
 * @author SUM
 * @date 2026/03/29
 */
public final class PayCallbackRequestConvert {

    /**
     * {@link PayCallbackCommand} to {@link PayCallbackRequest}
     *
     * @param payCallbackCommand 支付回调请求参数
     * @return {@link PayCallbackRequest}
     */
    public static PayCallbackRequest command2PayCallbackRequest(PayCallbackCommand payCallbackCommand) {
        PayCallbackRequest payCallbackRequest = null;
        if (Objects.equals(payCallbackCommand.getChannel(), PayChannelEnum.ALI_PAY.getCode())) {
            payCallbackRequest = BeanUtil.convert(payCallbackCommand, AliPayCallbackRequest.class);
            ((AliPayCallbackRequest) payCallbackRequest).setOrderRequestId(payCallbackCommand.getOrderRequestId());
        }
        return payCallbackRequest;
    }
}
