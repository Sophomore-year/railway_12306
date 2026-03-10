package org.zys.railway_12306.framework.starter.web.initialize;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.zys.railway_12306.framework.starter.web.config.WebAutoConfiguration.INITIALIZE_PATH;

/**
 *初始化 {@link org.springframework.web.servlet.DispatcherServlet}
 *
 * @author SUM
 * @date 2026/03/10
 */
@Slf4j(topic = "初始化 DispatcherServlet")
@RestController
public final class InitializeDispatcherServletController {
    @GetMapping(INITIALIZE_PATH)
    public void initializeDispatcherServlet() {
        log.info("已初始化DispatcherServlet以提升接口首次响应时间...");
    }
}
