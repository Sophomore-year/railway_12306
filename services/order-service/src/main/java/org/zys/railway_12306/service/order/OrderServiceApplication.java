package org.zys.railway_12306.service.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 订单服务应用启动器
 *
 * @author SUM
 * @date 2026/03/27
 */
@SpringBootApplication
@MapperScan("org.zys.railway_12306.service.order.mapper")
@EnableFeignClients(basePackages = "org.zys.railway_12306.service.order.remote")
public class OrderServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(OrderServiceApplication.class, args);
    }
}
