package org.zys.railway_12306.service.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.service.order.mapper.OrderItemMapper;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderItemQueryReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.zys.railway_12306.service.order.pojo.entity.OrderItem;
import org.zys.railway_12306.service.order.service.OrderItemService;

import java.util.List;

/**
 *订单明细接口层实现
 *
 * @author SUM
 * @date 2026/03/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemMapper orderItemMapper;

    @Override
    public List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam) {
        LambdaQueryWrapper<OrderItem> queryWrapper = Wrappers.lambdaQuery(OrderItem.class)
                .eq(OrderItem::getOrderSn, requestParam.getOrderSn())
                .in(OrderItem::getId, requestParam.getOrderItemRecordIds());
        List<OrderItem> orderItemList = orderItemMapper.selectList(queryWrapper);
        return BeanUtil.convert(orderItemList, TicketOrderPassengerDetailRespDTO.class);
    }
}
