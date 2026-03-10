package org.zys.rail_12306.framework.starter.idempotent.enums;

/**
 *幂等验证场景枚举
 *
 * @author SUM
 * @date 2026/03/10
 */
public enum IdempotentSceneEnum {
    /**
     * 基于 RestAPI 场景验证
     */
    RESTAPI,

    /**
     * 基于 MQ 场景验证
     */
    MQ
}
