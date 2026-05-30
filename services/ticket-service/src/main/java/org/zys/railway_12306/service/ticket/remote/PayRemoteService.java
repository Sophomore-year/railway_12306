package org.zys.railway_12306.service.ticket.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.service.ticket.remote.dto.PayInfoRespDTO;
import org.zys.railway_12306.service.ticket.remote.dto.RefundReqDTO;
import org.zys.railway_12306.service.ticket.remote.dto.RefundRespDTO;

@FeignClient(value = "railway12306-pay${unique-name:}-service")
public interface PayRemoteService {

    /**
     * 支付单详情查询
     */
    @GetMapping("/api/pay-service/pay/query")
    Result<PayInfoRespDTO> getPayInfo(@RequestParam(value = "orderSn") String orderSn);

    /**
     * 公共退款接口
     */
    @PostMapping("/api/pay-service/common/refund")
    Result<RefundRespDTO> commonRefund(@RequestBody RefundReqDTO requestParam);
}
