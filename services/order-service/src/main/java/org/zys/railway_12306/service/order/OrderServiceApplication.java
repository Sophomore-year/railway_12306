package org.zys.railway_12306.service.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单服务应用启动器
 *
 * @author SUM
 * @date 2026/03/27
 */
@SpringBootApplication
@MapperScan("org.zys.railway_12306.service.order.mapper")
public class OrderServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(OrderServiceApplication.class, args);
    }
}
