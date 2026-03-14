package org.zys.railway_12306.service.user.toolkit;

import static org.zys.railway_12306.service.user.constant.Railway12306Constant.USER_REGISTER_REUSE_SHARDING_COUNT;


/**
 *用户名可复用工具类
 *
 * @author SUM
 * @date 2026/03/14
 */
public class UserReuseUtil {
    /**
     * 计算分片位置
     */
    public static int hashShardingIdx(String username) {
        return Math.abs(username.hashCode() % USER_REGISTER_REUSE_SHARDING_COUNT);
    }
}
