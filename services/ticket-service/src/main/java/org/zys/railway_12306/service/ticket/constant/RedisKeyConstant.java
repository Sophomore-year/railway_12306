package org.zys.railway_12306.service.ticket.constant;

/**
 *Redis Key 定义常量类
 *
 * @author SUM
 * @date 2026/03/18
 */
public final class RedisKeyConstant {

    /**
     * 列车基本信息，Key Prefix + 列车ID
     */
    public static final String TRAIN_INFO = "railway_12306-ticket-service:train_info:";

    /**
     * 地区与站点映射查询
     * */
    public static final String REGION_TRAIN_STATION_MAPPING = "railway_12306-ticket-service:region_train_station_mapping";

    /**
     * 站点查询分布式锁 Key
     * */
    public static final String LOCK_REGION_TRAIN_STATION_MAPPING = "railway_12306-ticket-service:lock_region_train_station_mapping";

    /**
     * 站点查询，Key Prefix + 起始城市_终点城市_日期
     */
    public static final String REGION_TRAIN_STATION = "railway_12306-ticket-service:region_train_station:%s_%s";

    /**
     * 站点查询分布式锁 Key
     */
    public static final String LOCK_REGION_TRAIN_STATION = "railway_12306-ticket-service:lock:region_train_station";

    /**
     * 列车站点座位价格查询，Key Prefix + 列车ID_起始城市_终点城市
     */
    public static final String TRAIN_STATION_PRICE = "railway_12306-ticket-service:train_station_price:%s_%s_%s";

    /**
     * 站点余票查询，Key Prefix + 列车ID_起始站点_终点
     */
    public static final String TRAIN_STATION_REMAINING_TICKET = "railway_12306-ticket-service:train_station_remaining_ticket:";

    /**
     * 获取相邻座位余票分布式锁 Key
     */
    public static final String LOCK_SAFE_LOAD_SEAT_MARGIN_GET = "railway_12306-ticket-service:lock:safe_load_seat_margin_%s";

    /**
     * 用户购票分布式锁 Key
     */
    public static final String LOCK_PURCHASE_TICKETS = "${unique-name:}railway_12306-ticket-service:lock:purchase_tickets_%s";
    /**
     * 用户购票分布式锁 Key v2
     */
    public static final String LOCK_PURCHASE_TICKETS_V2 = "${unique-name:}railway_12306-ticket-service:lock:purchase_tickets_%s_%d";

    /**
     * 地区以及车站查询，Key Prefix + ( 车站名称 or 查询方式 )
     */
    public static final String REGION_STATION = "railway_12306-ticket-service:region-station:";

    /**
     * 获取地区以及站点集合分布式锁 Key
     */
    public static final String LOCK_QUERY_REGION_STATION_LIST = "railway_12306-ticket-service:lock:query_region_station_list_%s";

    /**
     * 列车站点缓存
     */
    public static final String STATION_ALL = "railway_12306-ticket-service:all_station";


    /**
     * 列车路线信息查询，Key Prefix + 列车ID
     */
    public static final String TRAIN_STATION_STOPOVER_DETAIL = "railway_12306-ticket-service:train_station_stopover_detail:";


    /**
     * 列车购买令牌桶，Key Prefix + 列车ID
     */
    public static final String TICKET_AVAILABILITY_TOKEN_BUCKET = "railway_12306-ticket-service:ticket_availability_token_bucket:";

    /**
     * 列车购买令牌桶加载数据 Key
     */
    public static final String LOCK_TICKET_AVAILABILITY_TOKEN_BUCKET = "railway_12306-ticket-service:lock:ticket_availability_token_bucket:%s";
    /**
     * 令牌获取失败分布式锁 Key
     */
    public static final String LOCK_TOKEN_BUCKET_ISNULL = "railway_12306-ticket-service:lock:token-bucket-isnull:%s";

    /**
     * 获取全部地点集合分布式锁 Key
     */
    public static final String LOCK_QUERY_ALL_REGION_LIST = "railway_12306-ticket-service:lock:query_all_region_list";

    /**
     * 获取全部地点集合 Key
     */
    public static final String QUERY_ALL_REGION_LIST = "railway_12306-ticket-service:query_all_region_list";

    /**
     * 车厢余票查询，Key Prefix + 列车ID_起始站点_终点
     */
    public static final String TRAIN_STATION_CARRIAGE_REMAINING_TICKET = "railway_12306-ticket-service:train_station_carriage_remaining_ticket:";

    /**
     * 取消/关闭订单后释放座位分布式锁 Key，Key Prefix + 订单号
     * <p>用于保证同一订单的座位释放逻辑只被执行一次，防止手动取消与延时关单并发触发重复恢复余票</p>
     */
    public static final String LOCK_RELEASE_SEAT = "railway_12306-ticket-service:lock:release_seat_%s";

}
