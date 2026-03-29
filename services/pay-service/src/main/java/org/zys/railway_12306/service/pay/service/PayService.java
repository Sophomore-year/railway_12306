package org.zys.railway_12306.service.pay.service;

import org.zys.railway_12306.service.pay.pojo.dto.PayInfoRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.PayRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.RefundReqDTO;
import org.zys.railway_12306.service.pay.pojo.dto.RefundRespDTO;
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


    /**
     * 跟据支付流水号查询支付单详情
     *
     * @param paySn 支付单流水号
     * @return 支付单详情
     */
    PayInfoRespDTO getPayInfoByPaySn(String paySn);

    /**
     * 公共退款接口
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    RefundRespDTO commonRefund(RefundReqDTO requestParam);

}
