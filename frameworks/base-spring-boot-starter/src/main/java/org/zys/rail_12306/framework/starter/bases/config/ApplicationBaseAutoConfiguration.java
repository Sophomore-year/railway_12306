package org.zys.rail_12306.framework.starter.bases.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.zys.rail_12306.framework.starter.bases.init.ApplicationContentPostProcessor;
import org.zys.rail_12306.framework.starter.bases.safa.FastJsonSafeMode;

/**
 * 应用基础自动装配
 *
 * @author SUM
 * @date 2026/03/16
 */
public class ApplicationBaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "framework.fastjson.safe-mode", havingValue = "true")
    public FastJsonSafeMode congoFastJsonSafeMode() {
        return new FastJsonSafeMode();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContentPostProcessor applicationContentPostProcessor(ApplicationContext applicationContext) {
        return new ApplicationContentPostProcessor(applicationContext);
    }
}
