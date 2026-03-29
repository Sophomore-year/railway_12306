package org.zys.rail_12306.framework.starter.cache.core;

/**
 *缓存过滤
 *
 * @author SUM
 * @date 2026/03/11
 */
@FunctionalInterface
public interface CacheGetFilter<T> {
    /**
     * 缓存过滤
     *
     * @param param 输出参数
     * @return {@code true} 如果输入参数匹配，否则 {@link Boolean#TRUE}
     */
    boolean filter(T param);
}
