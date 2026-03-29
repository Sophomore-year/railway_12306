package org.zys.rail_12306.framework.starter.cache.core;


/**
 *缓存加载器
 *
 * @author SUM
 * @date 2026/03/10
 */
@FunctionalInterface
public interface CacheLoader<T> {
    /**
     * 加载缓存
     */
    T load();
}
