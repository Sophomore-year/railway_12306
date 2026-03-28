package org.zys.railway_12306.service.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.service.order.mapper.OrderItemMapper;
import org.zys.railway_12306.service.order.mapper.OrderMapper;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderDetailRespDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.zys.railway_12306.service.order.pojo.entity.Order;
import org.zys.railway_12306.service.order.pojo.entity.OrderItem;
import org.zys.railway_12306.service.order.service.OrderService;

import java.util.List;


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
}
