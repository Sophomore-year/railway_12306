package org.zys.railway_12306.service.pay.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.web.Results;
import org.zys.railway_12306.service.pay.pojo.dto.RefundReqDTO;
import org.zys.railway_12306.service.pay.pojo.dto.RefundRespDTO;
import org.zys.railway_12306.service.pay.service.RefundService;

/**
 *退款控制层
 *
 * @author SUM
 * @date 2026/05/30
 */
@RestController
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    /**
     * 公共退款接口
     */
    @PostMapping("/api/pay-service/common/refund")
    public Result<RefundRespDTO> commonRefund(@RequestBody RefundReqDTO requestParam) {
        return Results.success(refundService.commonRefund(requestParam));
    }
}
