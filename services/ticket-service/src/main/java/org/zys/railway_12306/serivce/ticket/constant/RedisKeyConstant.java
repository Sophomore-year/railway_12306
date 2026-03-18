package org.zys.railway_12306.serivce.ticket.constant;

/**
 *Redis Key 定义常量类
 *
 * @author SUM
 * @date 2026/03/18
 */
public final class RedisKeyConstant {

    /**
     * 地区与站点映射查询
     * */
    public static final String REGION_TRAIN_STATION_MAPPING = "railway_12306-ticket-service:region_train_station_mapping";

    /**
     * 站点查询分布式锁 Key
     * */
    public static final String LOCK_REGION_TRAIN_STATION_MAPPING = "railway_12306-ticket-service:lock_region_train_station_mapping";

}
