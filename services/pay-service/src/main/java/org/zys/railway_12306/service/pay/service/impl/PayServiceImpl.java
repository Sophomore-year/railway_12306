package org.zys.railway_12306.service.pay.service.impl;


import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.rail_12306.framework.starter.designpattern.strategy.AbstractStrategyChoose;
import org.zys.rail_12306.framework.starter.idempotent.annotation.Idempotent;
import org.zys.rail_12306.framework.starter.idempotent.enums.IdempotentTypeEnum;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.service.pay.enums.TradeStatusEnum;
import org.zys.railway_12306.service.pay.mapper.PayMapper;
import org.zys.railway_12306.service.pay.pojo.dao.entity.Pay;
import org.zys.railway_12306.service.pay.pojo.dto.PayInfoRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.PayRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.base.PayRequest;
import org.zys.railway_12306.service.pay.pojo.dto.base.PayResponse;
import org.zys.railway_12306.service.pay.service.PayService;
import org.zys.railway_12306.service.pay.service.payid.PayIdGeneratorManager;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.zys.railway_12306.service.pay.constant.RedisKeyConstant.ORDER_PAY_RESULT_INFO;

/**
 *支付接口层实现
 *
 * @author SUM
 * @date 2026/03/29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final DistributedCache distributedCache;
    private final AbstractStrategyChoose abstractStrategyChoose;
    private final PayMapper payMapper;

    @Idempotent(
            type = IdempotentTypeEnum.SPEL,
            uniqueKeyPrefix = "railway_12306-pay:lock_create_pay:",
            key = "#requestParam.getOutOrderSn()"
    )
    @Transactional(rollbackFor = Exception.class)
    @Override
    public PayRespDTO commonPay(PayRequest requestParam) {
        PayRespDTO cacheResult = distributedCache.get(ORDER_PAY_RESULT_INFO + requestParam.getOrderSn(), PayRespDTO.class);
        if (cacheResult != null) {
            return cacheResult;
        }
        /**
         * {@link AliPayNativeHandler}
         */
        // 策略模式：通过策略模式封装支付渠道和支付场景，用户支付时动态选择对应的支付组件
        PayResponse result = abstractStrategyChoose.chooseAndExecuteResp(requestParam.buildMark(), requestParam);
        Pay insertPay = BeanUtil.convert(requestParam, Pay.class);
        String paySn = PayIdGeneratorManager.generateId(requestParam.getOrderSn());
        insertPay.setPaySn(paySn);
        insertPay.setStatus(TradeStatusEnum.WAIT_BUYER_PAY.tradeCode());
        insertPay.setTotalAmount(requestParam.getTotalAmount().multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        int insert = payMapper.insert(insertPay);
        if (insert <= 0) {
            log.error("支付单创建失败，支付聚合根：{}", JSON.toJSONString(requestParam));
            throw new ServiceException("支付单创建失败");
        }
        distributedCache.put(ORDER_PAY_RESULT_INFO + requestParam.getOrderSn(), JSON.toJSONString(result), 10, TimeUnit.MINUTES);
        return BeanUtil.convert(result, PayRespDTO.class);
    }

    @Override
    public PayInfoRespDTO getPayInfoByOrderSn(String orderSn) {
        LambdaQueryWrapper<Pay> queryWrapper = Wrappers.lambdaQuery(Pay.class)
                .eq(Pay::getOrderSn, orderSn);
        Pay pay = payMapper.selectOne(queryWrapper);
        return BeanUtil.convert(pay, PayInfoRespDTO.class);
    }
}
