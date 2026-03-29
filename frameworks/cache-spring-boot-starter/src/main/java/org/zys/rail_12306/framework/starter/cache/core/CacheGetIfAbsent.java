package org.zys.rail_12306.framework.starter.cache.core;

/**
 *缓存查询为空
 *
 * @author SUM
 * @date 2026/03/11
 */
@FunctionalInterface
public interface CacheGetIfAbsent <T>{
    /**
     * 如果查询结果为空，执行逻辑
     */
    void execute(T param);
}
