package org.zys.railway_12306.service.ticket;


import cn.hippo4j.core.enable.EnableDynamicThreadPool;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 *购票服务应用启动器
 *
 * @author SUM
 * @date 2026/03/22
 */
@SpringBootApplication
@EnableDynamicThreadPool
@MapperScan("org.zys.railway_12306.serivce.ticket.mapper")
@EnableFeignClients("org.zys.railway_12306.serivce.ticket.remote")//
public class TicketServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(TicketServiceApplication.class, args);
    }
}
