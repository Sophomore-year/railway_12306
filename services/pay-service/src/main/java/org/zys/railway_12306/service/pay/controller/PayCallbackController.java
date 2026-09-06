package org.zys.railway_12306.service.pay.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zys.rail_12306.framework.starter.designpattern.strategy.AbstractStrategyChoose;
import org.zys.railway_12306.service.pay.convert.PayCallbackRequestConvert;
import org.zys.railway_12306.service.pay.enums.PayChannelEnum;
import org.zys.railway_12306.service.pay.pojo.dto.PayCallbackCommand;
import org.zys.railway_12306.service.pay.pojo.dto.base.AliPayCallbackRequest;
import org.zys.railway_12306.service.pay.pojo.dto.base.PayCallbackRequest;

import java.util.HashMap;
import java.util.Map;

/**
 *支付结果回调
 *
 * @author SUM
 * @date 2026/03/29
 */
@RestController
@RequiredArgsConstructor
public class PayCallbackController {

    private final AbstractStrategyChoose abstractStrategyChoose;

    /**
     * 支付宝回调
     * 调用支付宝支付后，支付宝会调用此接口发送支付结果
     */
    @PostMapping("/api/pay-service/callback/alipay")
    public void callbackAlipay(@RequestParam Map<String, Object> requestParam) {
        PayCallbackCommand payCallbackCommand = BeanUtil.mapToBean(requestParam, PayCallbackCommand.class, true, CopyOptions.create());
        payCallbackCommand.setChannel(PayChannelEnum.ALI_PAY.getCode());
        payCallbackCommand.setOrderRequestId(requestParam.get("out_trade_no").toString());
        payCallbackCommand.setGmtPayment(DateUtil.parse(requestParam.get("gmt_payment").toString()));
        PayCallbackRequest payCallbackRequest = PayCallbackRequestConvert.command2PayCallbackRequest(payCallbackCommand);
        // 携带回调原始参数（含 sign），供支付回调策略执行 RSA2 验签
        if (payCallbackRequest instanceof AliPayCallbackRequest aliPayCallbackRequest) {
            Map<String, String> originParams = new HashMap<>();
            requestParam.forEach((key, value) -> originParams.put(key, value == null ? null : value.toString()));
            aliPayCallbackRequest.setOriginParams(originParams);
        }
        /**
         * {@link AliPayCallbackHandler}
         */
        // 策略模式：通过策略模式封装支付回调渠道，支付回调时动态选择对应的支付回调组件
        abstractStrategyChoose.chooseAndExecute(payCallbackRequest.buildMark(), payCallbackRequest);
    }
}
