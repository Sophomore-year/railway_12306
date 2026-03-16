package org.zys.railway_12306.service.user.constant;

/**
 *Redis Key 定义常量类
 *
 * @author SUM
 * @date 2026/03/14
 */
public final class RedisKeyConstant {
    /**
     * 用户注册锁，Key Prefix + 用户名
     */
    public static final String LOCK_USER_REGISTER = "railway_12306-user-service:lock:user-register:";

    /**
     * 用户注销锁，Key Prefix + 用户名
     */
    public static final String USER_DELETION = "railway_12306-user-service:user-deletion:";

    /**
     * 用户注册可复用用户名分片，Key Prefix + Idx
     */
    public static final String USER_REGISTER_REUSE_SHARDING = "railway_12306-user-service:user-reuse:";

    /**
     * 用户乘车人列表，Key Prefix + 用户名
     */
    public static final String USER_PASSENGER_LIST = "railway_12306-user-service:user-passenger-list:";
    
    /**
     * 用户删除次数分片，Key Prefix + Idx
     */
    public static final String USER_DELETION_COUNT_SHARDING = "railway_12306-user-service:user-deletion-count:";
}
