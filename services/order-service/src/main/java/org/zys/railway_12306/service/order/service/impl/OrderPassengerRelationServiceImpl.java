package org.zys.railway_12306.service.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.zys.railway_12306.service.order.mapper.OrderItemPassengerMapper;
import org.zys.railway_12306.service.order.pojo.entity.OrderItemPassenger;
import org.zys.railway_12306.service.order.service.OrderPassengerRelationService;

/**
 *乘车人订单关系接口层实现
 *
 * @author SUM
 * @date 2026/03/28
 */
@Service
public class OrderPassengerRelationServiceImpl extends ServiceImpl<OrderItemPassengerMapper, OrderItemPassenger> implements OrderPassengerRelationService {
}
