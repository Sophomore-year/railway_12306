package org.zys.railway_12306.service.order.service.impl;

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.StrBuilder;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zys.rail_12306.framework.starter.database.toolkit.PageUtil;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.framework.starter.convention.page.PageResponse;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.user.core.UserContext;
import org.zys.railway_12306.service.order.enums.OrderCanalErrorCodeEnum;
import org.zys.railway_12306.service.order.enums.OrderItemStatusEnum;
import org.zys.railway_12306.service.order.enums.OrderStatusEnum;
import org.zys.railway_12306.service.order.mapper.OrderItemMapper;
import org.zys.railway_12306.service.order.mapper.OrderMapper;
import org.zys.railway_12306.service.order.mq.event.DelayCloseOrderEvent;
import org.zys.railway_12306.service.order.mq.produce.DelayCloseOrderSendProduce;
import org.zys.railway_12306.service.order.pojo.dto.req.CancelTicketOrderReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderCreateReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderItemCreateReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderPageQueryReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderSelfPageQueryReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderDetailRespDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderDetailSelfRespDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.zys.railway_12306.service.order.pojo.entity.Order;
import org.zys.railway_12306.service.order.pojo.entity.OrderItem;
import org.zys.railway_12306.service.order.pojo.entity.OrderItemPassenger;
import org.zys.railway_12306.service.order.remote.UserRemoteService;
import org.zys.railway_12306.service.order.remote.dto.UserQueryActualRespDTO;
import org.zys.railway_12306.service.order.service.OrderItemService;
import org.zys.railway_12306.service.order.service.OrderPassengerRelationService;
import org.zys.railway_12306.service.order.service.OrderService;
import org.zys.railway_12306.service.order.service.orderid.OrderIdGeneratorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 *订单服务接口层实现
 *
 * @author SUM
 * @date 2026/03/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserRemoteService userRemoteService;
    private final OrderPassengerRelationService orderPassengerRelationService;
    private final OrderItemService orderItemService;
    private final DelayCloseOrderSendProduce delayCloseOrderSendProduce;
    private final RedissonClient redissonClient;


    @Override
    public TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn) {
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn);
        Order order = orderMapper.selectOne(queryWrapper);
        TicketOrderDetailRespDTO result = BeanUtil.convert(order, TicketOrderDetailRespDTO.class);
        LambdaQueryWrapper<OrderItem> orderItemQueryWrapper = Wrappers.lambdaQuery(OrderItem.class)
                .eq(OrderItem::getOrderSn, orderSn);
        List<OrderItem> orderItemDOList = orderItemMapper.selectList(orderItemQueryWrapper);
        result.setPassengerDetails(BeanUtil.convert(orderItemDOList, TicketOrderPassengerDetailRespDTO.class));
        return result;
    }

    @AutoOperate(type = TicketOrderDetailRespDTO.class, on = "data.records")
    @Override
    public PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam) {
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getUserId, requestParam.getUserId())
                .in(Order::getStatus, buildOrderStatusList(requestParam))
                .orderByDesc(Order::getOrderTime);
        IPage<Order> orderPage = orderMapper.selectPage(PageUtil.convert(requestParam), queryWrapper);
        return PageUtil.convert(orderPage, each -> {
            TicketOrderDetailRespDTO result = BeanUtil.convert(each, TicketOrderDetailRespDTO.class);
            LambdaQueryWrapper<OrderItem> orderItemQueryWrapper = Wrappers.lambdaQuery(OrderItem.class)
                    .eq(OrderItem::getOrderSn, each.getOrderSn());
            List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemQueryWrapper);
            result.setPassengerDetails(BeanUtil.convert(orderItemList, TicketOrderPassengerDetailRespDTO.class));
            return result;
        });
    }


    @Override
    public PageResponse<TicketOrderDetailSelfRespDTO> pageSelfTicketOrder(TicketOrderSelfPageQueryReqDTO requestParam) {
        Result<UserQueryActualRespDTO> userActualResp = userRemoteService.queryActualUserByUsername(UserContext.getUsername());
        LambdaQueryWrapper<OrderItemPassenger> queryWrapper = Wrappers.lambdaQuery(OrderItemPassenger.class)
                .eq(OrderItemPassenger::getIdCard, userActualResp.getData().getIdCard())
                .orderByDesc(OrderItemPassenger::getCreateTime);
        IPage<OrderItemPassenger> orderItemPassengerPage = orderPassengerRelationService.page(PageUtil.convert(requestParam), queryWrapper);
        return PageUtil.convert(orderItemPassengerPage, each -> {
            LambdaQueryWrapper<Order> orderQueryWrapper = Wrappers.lambdaQuery(Order.class)
                    .eq(Order::getOrderSn, each.getOrderSn());
            Order orderDO = orderMapper.selectOne(orderQueryWrapper);
            LambdaQueryWrapper<OrderItem> orderItemQueryWrapper = Wrappers.lambdaQuery(OrderItem.class)
                    .eq(OrderItem::getOrderSn, each.getOrderSn())
                    .eq(OrderItem::getIdCard, each.getIdCard());
            OrderItem orderItem = orderItemMapper.selectOne(orderItemQueryWrapper);
            TicketOrderDetailSelfRespDTO actualResult = BeanUtil.convert(orderDO, TicketOrderDetailSelfRespDTO.class);
            BeanUtil.convertIgnoreNullAndBlank(orderItem, actualResult);
            return actualResult;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createTicketOrder(TicketOrderCreateReqDTO requestParam) {
        // 通过基因法(订单 ID 全局唯一生成)将用户 ID 融入到订单号
        String orderSn = OrderIdGeneratorManager.generateId(requestParam.getUserId());
        Order order = Order.builder().orderSn(orderSn)
                .orderTime(requestParam.getOrderTime())
                .departure(requestParam.getDeparture())
                .departureTime(requestParam.getDepartureTime())
                .ridingDate(requestParam.getRidingDate())
                .arrivalTime(requestParam.getArrivalTime())
                .trainNumber(requestParam.getTrainNumber())
                .arrival(requestParam.getArrival())
                .trainId(requestParam.getTrainId())
                .source(requestParam.getSource())
                .status(OrderStatusEnum.PENDING_PAYMENT.getStatus())
                .username(requestParam.getUsername())
                .userId(String.valueOf(requestParam.getUserId()))
                .build();
        orderMapper.insert(order);
        // 订单明细列表
        List<TicketOrderItemCreateReqDTO> ticketOrderItems = requestParam.getTicketOrderItems();
        List<OrderItem> orderItemList = new ArrayList<>();
        List<OrderItemPassenger> orderPassengerRelationList = new ArrayList<>();
        ticketOrderItems.forEach(each -> {
            OrderItem orderItem = OrderItem.builder()
                    .trainId(requestParam.getTrainId())
                    .seatNumber(each.getSeatNumber())
                    .carriageNumber(each.getCarriageNumber())
                    .realName(each.getRealName())
                    .orderSn(orderSn)
                    .phone(each.getPhone())
                    .seatType(each.getSeatType())
                    .username(requestParam.getUsername())
                    .amount(each.getAmount())
                    .carriageNumber(each.getCarriageNumber())
                    .idCard(each.getIdCard())
                    .ticketType(each.getTicketType())
                    .idType(each.getIdType())
                    .userId(String.valueOf(requestParam.getUserId()))
                    .status(0)
                    .build();
            orderItemList.add(orderItem);
            OrderItemPassenger orderPassengerRelationDO = OrderItemPassenger.builder()
                    .idType(each.getIdType())
                    .idCard(each.getIdCard())
                    .orderSn(orderSn)
                    .build();
            orderPassengerRelationList.add(orderPassengerRelationDO);
        });
        orderItemService.saveBatch(orderItemList);
        orderPassengerRelationService.saveBatch(orderPassengerRelationList);
        try {
            // 发送 RocketMQ 延时消息，指定时间后取消订单
            DelayCloseOrderEvent delayCloseOrderEvent = DelayCloseOrderEvent.builder()
                    .trainId(String.valueOf(requestParam.getTrainId()))
                    .departure(requestParam.getDeparture())
                    .arrival(requestParam.getArrival())
                    .orderSn(orderSn)
                    .trainPurchaseTicketResults(requestParam.getTicketOrderItems())
                    .build();
            // 创建订单并支付后延时关闭订单消息怎么办？
            SendResult sendResult = delayCloseOrderSendProduce.sendMessage(delayCloseOrderEvent);
            if (!Objects.equals(sendResult.getSendStatus(), SendStatus.SEND_OK)) {
                throw new ServiceException("投递延迟关闭订单消息队列失败");
            }
        } catch (Throwable ex) {
            log.error("延迟关闭订单消息队列发送错误，请求参数：{}", JSON.toJSONString(requestParam), ex);
            throw ex;
        }
        return orderSn;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean closeTickOrder(CancelTicketOrderReqDTO requestParam) {
        String orderSn = requestParam.getOrderSn();
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn)
                .select(Order::getStatus);
        Order order = orderMapper.selectOne(queryWrapper);
        // 订单不存在或者订单状态不是待支付状态
        if (Objects.isNull(order) || order.getStatus() != OrderStatusEnum.PENDING_PAYMENT.getStatus()) {
            return false;
        }
        // 原则上订单关闭和订单取消这两个方法可以复用，为了区分未来考虑到的场景，这里对方法进行拆分但复用逻辑
        return cancelTickOrder(requestParam);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelTickOrder(CancelTicketOrderReqDTO requestParam) {
        String orderSn = requestParam.getOrderSn();
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn);
        Order order = orderMapper.selectOne(queryWrapper);
        if (order == null) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_UNKNOWN_ERROR);
        } else if (order.getStatus() != OrderStatusEnum.PENDING_PAYMENT.getStatus()) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_STATUS_ERROR);
        }
        RLock lock = redissonClient.getLock(StrBuilder.create("order:canal:order_sn_").append(orderSn).toString());
        if (!lock.tryLock()) {
            throw new ClientException(OrderCanalErrorCodeEnum.ORDER_CANAL_REPETITION_ERROR);
        }
        try {
            Order updateOrder = new Order();
            updateOrder.setStatus(OrderStatusEnum.CLOSED.getStatus());
            LambdaUpdateWrapper<Order> updateWrapper = Wrappers.lambdaUpdate(Order.class)
                    .eq(Order::getOrderSn, orderSn);
            int updateResult = orderMapper.update(updateOrder, updateWrapper);
            if (updateResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_ERROR);
            }
            OrderItem updateOrderItem = new OrderItem();
            updateOrderItem.setStatus(OrderItemStatusEnum.CLOSED.getStatus());
            LambdaUpdateWrapper<OrderItem> updateItemWrapper = Wrappers.lambdaUpdate(OrderItem.class)
                    .eq(OrderItem::getOrderSn, orderSn);
            int updateItemResult = orderItemMapper.update(updateOrderItem, updateItemWrapper);
            if (updateItemResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_ERROR);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    private List<Integer> buildOrderStatusList(TicketOrderPageQueryReqDTO requestParam) {
        List<Integer> result = new ArrayList<>();
        switch (requestParam.getStatusType()) {
            case 0 -> result = ListUtil.of(
                    OrderStatusEnum.PENDING_PAYMENT.getStatus()
            );
            case 1 -> result = ListUtil.of(
                    OrderStatusEnum.ALREADY_PAID.getStatus(),
                    OrderStatusEnum.PARTIAL_REFUND.getStatus(),
                    OrderStatusEnum.FULL_REFUND.getStatus()
            );
            case 2 -> result = ListUtil.of(
                    OrderStatusEnum.COMPLETED.getStatus()
            );
        }
        return result;
    }
}
