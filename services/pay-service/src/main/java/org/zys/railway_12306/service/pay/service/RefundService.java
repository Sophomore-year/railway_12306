package org.zys.railway_12306.service.pay.service;

import org.zys.railway_12306.service.pay.pojo.dto.RefundReqDTO;
import org.zys.railway_12306.service.pay.pojo.dto.RefundRespDTO;

/**
 *退款接口层
 *
 * @author SUM
 * @date 2026/05/30
 */
public interface RefundService {

    /**
     * 公共退款接口
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    RefundRespDTO commonRefund(RefundReqDTO requestParam);
}
