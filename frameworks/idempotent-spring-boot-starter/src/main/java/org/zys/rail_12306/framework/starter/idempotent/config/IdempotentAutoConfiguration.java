package org.zys.rail_12306.framework.starter.idempotent.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.rail_12306.framework.starter.idempotent.core.IdempotentAspect;
import org.zys.rail_12306.framework.starter.idempotent.core.param.IdempotentParamExecuteHandler;
import org.zys.rail_12306.framework.starter.idempotent.core.param.IdempotentParamService;
import org.zys.rail_12306.framework.starter.idempotent.core.spel.IdempotentSpELByMQExecuteHandler;
import org.zys.rail_12306.framework.starter.idempotent.core.spel.IdempotentSpELByRestAPIExecuteHandler;
import org.zys.rail_12306.framework.starter.idempotent.core.spel.IdempotentSpELService;
import org.zys.rail_12306.framework.starter.idempotent.core.token.IdempotentTokenController;
import org.zys.rail_12306.framework.starter.idempotent.core.token.IdempotentTokenExecuteHandler;
import org.zys.rail_12306.framework.starter.idempotent.core.token.IdempotentTokenService;

/**
 *幂等自动装配
 *
 * @author SUM
 * @date 2026/03/16
 */
@EnableConfigurationProperties(IdempotentProperties.class)
public class IdempotentAutoConfiguration {
    /**
     * 幂等切面
     */
    @Bean
    public IdempotentAspect idempotentAspect() {
        return new IdempotentAspect();
    }

    /**
     * 参数方式幂等实现，基于 RestAPI 场景
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentParamService idempotentParamExecuteHandler(RedissonClient redissonClient) {
        return new IdempotentParamExecuteHandler(redissonClient);
    }

    /**
     * Token 方式幂等实现，基于 RestAPI 场景
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentTokenService idempotentTokenExecuteHandler(DistributedCache distributedCache,
                                                                IdempotentProperties idempotentProperties) {
        return new IdempotentTokenExecuteHandler(distributedCache, idempotentProperties);
    }

    /**
     * 申请幂等 Token 控制器，基于 RestAPI 场景
     */
    @Bean
    public IdempotentTokenController idempotentTokenController(IdempotentTokenService idempotentTokenService) {
        return new IdempotentTokenController(idempotentTokenService);
    }

    /**
     * SpEL 方式幂等实现，基于 RestAPI 场景
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentSpELService idempotentSpELByRestAPIExecuteHandler(RedissonClient redissonClient) {
        return new IdempotentSpELByRestAPIExecuteHandler(redissonClient);
    }

    /**
     * SpEL 方式幂等实现，基于 MQ 场景
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentSpELByMQExecuteHandler idempotentSpELByMQExecuteHandler(DistributedCache distributedCache) {
        return new IdempotentSpELByMQExecuteHandler(distributedCache);
    }
}
