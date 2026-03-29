package org.zys.rail_12306.framework.starter.log.config;

import org.springframework.context.annotation.Bean;
import org.zys.rail_12306.framework.starter.log.annotation.ILog;
import org.zys.rail_12306.framework.starter.log.core.ILogPrintAspect;

/**
 *日志自动装配
 *
 * @author SUM
 * @date 2026/03/16
 */
public class LogAutoConfiguration {
    /**
     * {@link ILog} 日志打印 AOP 切面
     */
    @Bean
    public ILogPrintAspect iLogPrintAspect() {
        return new ILogPrintAspect();
    }
}
