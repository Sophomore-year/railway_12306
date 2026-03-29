package org.zys.railway_12306.service.pay.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.web.Results;
import org.zys.railway_12306.service.pay.convert.PayRequestConvert;
import org.zys.railway_12306.service.pay.pojo.dto.PayCommand;
import org.zys.railway_12306.service.pay.pojo.dto.PayInfoRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.PayRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.base.PayRequest;
import org.zys.railway_12306.service.pay.service.PayService;

/**
 *支付控制层
 *
 * @author SUM
 * @date 2026/03/29
 */
@RestController
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /**
     * 公共支付接口
     * 对接常用支付方式，比如：支付宝、微信以及银行卡等
     */
    @PostMapping("/api/pay-service/pay/create")
    public Result<PayRespDTO> pay(@RequestBody PayCommand requestParam) {
        PayRequest payRequest = PayRequestConvert.command2PayRequest(requestParam);
        PayRespDTO result = payService.commonPay(payRequest);
        return Results.success(result);
    }

    /**
     * 跟据订单号查询支付单详情
     */
    @GetMapping("/api/pay-service/pay/query/order-sn")
    public Result<PayInfoRespDTO> getPayInfoByOrderSn(@RequestParam(value = "orderSn") String orderSn) {
        return Results.success(payService.getPayInfoByOrderSn(orderSn));
    }
}
