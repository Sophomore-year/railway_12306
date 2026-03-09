package org.zys.rail_12306.framework.starter.bases.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class ApplicationContentPostProcessor implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationContext applicationContext;

    /*
     * 执行标识，确保Spring事件 {@link ApplicationReadyEvent} 有且执行一次
     */
    private final AtomicBoolean executeOnlyOnce = new AtomicBoolean(false);
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 使用 CAS 操作确保只执行一次
        if (!executeOnlyOnce.compareAndSet(false, true)) {
            return;  // 已经执行过，直接返回
        }
        // 只执行一次的逻辑
        applicationContext.publishEvent(new ApplicationInitializingEvent(this));

    }
}
