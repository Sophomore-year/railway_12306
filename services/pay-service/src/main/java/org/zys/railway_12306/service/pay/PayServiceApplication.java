package org.zys.railway_12306.service.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("org.zys.railway_12306.service.pay.mapper")
@EnableFeignClients(basePackages = "org.zys.railway_12306.service.pay.remote")
public class PayServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(PayServiceApplication.class, args);
    }
}
