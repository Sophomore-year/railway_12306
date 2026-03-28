package org.zys.railway_12306.service.order.constant;

/**
 *RocketMQ 订单服务常量类
 *
 * @author SUM
 * @date 2026/03/28
 */
public class OrderRocketMQConstant {

    /**
     * 订单服务相关业务 Topic Key
     */
    public static final String ORDER_DELAY_CLOSE_TOPIC_KEY = "railway_12306_order-service_delay-close-order_topic${unique-name:}";

    /**
     * 购票服务创建订单后延时关闭业务 Tag Key
     */
    public static final String ORDER_DELAY_CLOSE_TAG_KEY = "railway_12306_order-service_delay-close-order_tag${unique-name:}";
}
