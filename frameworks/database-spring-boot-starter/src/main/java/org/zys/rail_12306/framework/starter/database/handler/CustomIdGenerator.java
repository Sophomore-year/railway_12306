package org.zys.rail_12306.framework.starter.database.handler;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.zys.rail_12306.framework.starter.distributedid.toolkit.SnowflakeIdUtil;

/**
 *自定义雪花算法生成器
 *
 * @author SUM
 * @date 2026/03/10
 */
public class CustomIdGenerator implements IdentifierGenerator {
    @Override
    public Number nextId(Object entity) {
        return SnowflakeIdUtil.nextId();
    }
}
