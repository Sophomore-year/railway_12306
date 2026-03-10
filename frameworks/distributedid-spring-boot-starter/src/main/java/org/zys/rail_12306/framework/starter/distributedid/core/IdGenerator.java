package org.zys.rail_12306.framework.starter.distributedid.core;

/**
 *ID 生成器
 *
 * @author SUM
 * @date 2026/03/10
 */
public interface IdGenerator {
    /**
     * 下一个 ID
     */
    default long nextId() {
        return 0L;
    }

    /**
     * 下一个 ID 字符串
     */
    default String nextIdStr() {
        return "";
    }
}
