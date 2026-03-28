package org.zys.railway_12306.service.order.service.impl;

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zys.rail_12306.framework.starter.database.toolkit.PageUtil;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.page.PageResponse;
import org.zys.railway_12306.service.order.enums.OrderStatusEnum;
import org.zys.railway_12306.service.order.mapper.OrderItemMapper;
import org.zys.railway_12306.service.order.mapper.OrderMapper;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderPageQueryReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderDetailRespDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.zys.railway_12306.service.order.pojo.entity.Order;
import org.zys.railway_12306.service.order.pojo.entity.OrderItem;
import org.zys.railway_12306.service.order.service.OrderService;

import java.util.ArrayList;
import java.util.List;


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
