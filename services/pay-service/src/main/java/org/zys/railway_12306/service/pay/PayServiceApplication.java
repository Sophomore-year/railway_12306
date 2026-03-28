package org.zys.railway_12306.service.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.zys.railway_12306.service.pay.mapper")
public class PayServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(PayServiceApplication.class, args);
    }
}
