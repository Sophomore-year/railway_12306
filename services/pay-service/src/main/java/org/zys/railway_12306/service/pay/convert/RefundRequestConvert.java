package org.zys.railway_12306.service.pay.convert;

import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.service.pay.enums.PayChannelEnum;
import org.zys.railway_12306.service.pay.pojo.dto.RefundCommand;
import org.zys.railway_12306.service.pay.pojo.dto.base.AliRefundRequest;
import org.zys.railway_12306.service.pay.pojo.dto.base.RefundRequest;

import java.util.Objects;

/**
 *退款请求入参转换器
 *
 * @author SUM
 * @date 2026/03/29
 */
public final class RefundRequestConvert {

    /**
     * {@link RefundCommand} to {@link RefundRequest}
     *
     * @param refundCommand 退款请求参数
     * @return {@link RefundRequest}
     */
    public static RefundRequest command2RefundRequest(RefundCommand refundCommand) {
        RefundRequest refundRequest = null;
        if (Objects.equals(refundCommand.getChannel(), PayChannelEnum.ALI_PAY.getCode())) {
            refundRequest = BeanUtil.convert(refundCommand, AliRefundRequest.class);
        }
        return refundRequest;
    }
}
