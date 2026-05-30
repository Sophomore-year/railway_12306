package org.zys.railway_12306.service.pay.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zys.rail_12306.framework.starter.designpattern.strategy.AbstractStrategyChoose;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.service.pay.convert.RefundRequestConvert;
import org.zys.railway_12306.service.pay.enums.TradeStatusEnum;
import org.zys.railway_12306.service.pay.mapper.PayMapper;
import org.zys.railway_12306.service.pay.mapper.RefundMapper;
import org.zys.railway_12306.service.pay.pojo.dao.entity.Pay;
import org.zys.railway_12306.service.pay.pojo.dao.entity.Refund;
import org.zys.railway_12306.service.pay.pojo.dto.RefundCommand;
import org.zys.railway_12306.service.pay.pojo.dto.RefundCreateDTO;
import org.zys.railway_12306.service.pay.pojo.dto.RefundReqDTO;
import org.zys.railway_12306.service.pay.pojo.dto.RefundRespDTO;
import org.zys.railway_12306.service.pay.pojo.dto.base.RefundRequest;
import org.zys.railway_12306.service.pay.pojo.dto.base.RefundResponse;
import org.zys.railway_12306.service.pay.remote.TicketOrderRemoteService;
import org.zys.railway_12306.service.pay.remote.dto.TicketOrderDetailRespDTO;
import org.zys.railway_12306.service.pay.service.RefundService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;


/**
 *退款接口层实现
 *
 * @author SUM
 * @date 2026/05/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final PayMapper payMapper;
    private final RefundMapper refundMapper;
    private final TicketOrderRemoteService ticketOrderRemoteService;
    private final AbstractStrategyChoose abstractStrategyChoose;
    private final RefundResultCallbackOrderSendProduce refundResultCallbackOrderSendProduce;

    @Override
    @Transactional
    public RefundRespDTO commonRefund(RefundReqDTO requestParam) {
        RefundRespDTO refundRespDTO = null;
        LambdaQueryWrapper<Pay> queryWrapper = Wrappers.lambdaQuery(Pay.class)
                .eq(Pay::getOrderSn, requestParam.getOrderSn());
        Pay Pay = payMapper.selectOne(queryWrapper);
        if (Objects.isNull(Pay)) {
            log.error("支付单不存在，orderSn：{}", requestParam.getOrderSn());
            throw new ServiceException("支付单不存在");
        }
        Pay.setPayAmount(Pay.getTotalAmount() - requestParam.getRefundAmount());
        //创建退款单
        RefundCreateDTO refundCreateDTO = BeanUtil.convert(requestParam, RefundCreateDTO.class);
        refundCreateDTO.setPaySn(Pay.getPaySn());
        createRefund(refundCreateDTO);
        /**
         * {@link AliRefundNativeHandler}
         */
        // 策略模式：通过策略模式封装退款渠道和退款场景，用户退款时动态选择对应的退款组件
        RefundCommand refundCommand = BeanUtil.convert(Pay, RefundCommand.class);
        refundCommand.setPayAmount(new BigDecimal(requestParam.getRefundAmount()));
        RefundRequest refundRequest = RefundRequestConvert.command2RefundRequest(refundCommand);
        RefundResponse result = abstractStrategyChoose.chooseAndExecuteResp(refundRequest.buildMark(), refundRequest);
        Pay.setStatus(result.getStatus());
        LambdaUpdateWrapper<Pay> updateWrapper = Wrappers.lambdaUpdate(Pay.class)
                .eq(Pay::getOrderSn, requestParam.getOrderSn());
        int updateResult = payMapper.update(Pay, updateWrapper);
        if (updateResult <= 0) {
            log.error("修改支付单退款结果失败，支付单信息：{}", JSON.toJSONString(Pay));
            throw new ServiceException("修改支付单退款结果失败");
        }
        LambdaUpdateWrapper<Refund> refundUpdateWrapper = Wrappers.lambdaUpdate(Refund.class)
                .eq(Refund::getOrderSn, requestParam.getOrderSn());
        Refund refund = new Refund();
        refund.setTradeNo(result.getTradeNo());
        refund.setStatus(result.getStatus());
        int refundUpdateResult = refundMapper.update(refund, refundUpdateWrapper);
        if (refundUpdateResult <= 0) {
            log.error("修改退款单退款结果失败，退款单信息：{}", JSON.toJSONString(refund));
            throw new ServiceException("修改退款单退款结果失败");
        }
        // 退款成功，回调订单服务告知退款结果，修改订单流转状态
        if (Objects.equals(result.getStatus(), TradeStatusEnum.TRADE_CLOSED.tradeCode())) {
            RefundResultCallbackOrderEvent refundResultCallbackOrderEvent = RefundResultCallbackOrderEvent.builder()
                    .orderSn(requestParam.getOrderSn())
                    .refundTypeEnum(requestParam.getRefundTypeEnum())
                    .partialRefundTicketDetailList(requestParam.getRefundDetailReqDTOList())
                    .build();
            refundResultCallbackOrderSendProduce.sendMessage(refundResultCallbackOrderEvent);
        }
        //TODO 暂时返回空实体
        return refundRespDTO;
    }


    private void createRefund(RefundCreateDTO requestParam) {
        Result<TicketOrderDetailRespDTO> queryTicketResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(requestParam.getOrderSn());
        if (!queryTicketResult.isSuccess() && Objects.isNull(queryTicketResult.getData())) {
            throw new ServiceException("车票订单不存在");
        }
        TicketOrderDetailRespDTO orderDetailRespDTO = queryTicketResult.getData();
        requestParam.getRefundDetailReqDTOList().forEach(each -> {
            Refund refund = new Refund();
            refund.setPaySn(requestParam.getPaySn());
            refund.setOrderSn(requestParam.getOrderSn());
            refund.setTrainId(orderDetailRespDTO.getTrainId());
            refund.setTrainNumber(orderDetailRespDTO.getTrainNumber());
            refund.setDeparture(orderDetailRespDTO.getDeparture());
            refund.setArrival(orderDetailRespDTO.getArrival());
            refund.setDepartureTime(orderDetailRespDTO.getDepartureTime());
            refund.setArrivalTime(orderDetailRespDTO.getArrivalTime());
            refund.setRidingDate(orderDetailRespDTO.getRidingDate());
            refund.setSeatType(each.getSeatType());
            refund.setIdType(each.getIdType());
            refund.setIdCard(each.getIdCard());
            refund.setRealName(each.getRealName());
            refund.setRefundTime(new Date());
            refund.setAmount(each.getAmount());
            refund.setUserId(each.getUserId());
            refund.setUsername(each.getUsername());
            refundMapper.insert(refund);
        });
    }
}
