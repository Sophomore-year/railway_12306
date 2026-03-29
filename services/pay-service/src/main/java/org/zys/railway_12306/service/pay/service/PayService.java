package org.zys.railway_12306.service.pay.service;

import org.zys.railway_12306.service.pay.pojo.dto.PayInfoRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.PayRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.base.PayRequest;

/**
 *支付接口层
 *
 * @author SUM
 * @date 2026/03/29
 */
public interface PayService {

    /**
     * 创建支付单
     *
     * @param requestParam 创建支付单实体
     * @return 支付返回详情
     */
    PayRespDTO commonPay(PayRequest requestParam);

    /**
     * 跟据订单号查询支付单详情
     *
     * @param orderSn 订单号
     * @return 支付单详情
     */
    PayInfoRespDTO getPayInfoByOrderSn(String orderSn);
}
