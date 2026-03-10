package org.zys.railway_12306.framework.starter.common.toolkit;

import lombok.SneakyThrows;

/**
 *线程池工具类
 *
 * @author SUM
 * @date 2026/03/10
 */
public class ThreadUtil {
    /**
     * 睡眠当前线程指定时间 {@param millis}
     *
     * @param millis 睡眠时间，单位毫秒
     */
    @SneakyThrows(value = InterruptedException.class)
    public static void sleep(long millis) {
        Thread.sleep(millis);
    }
}
