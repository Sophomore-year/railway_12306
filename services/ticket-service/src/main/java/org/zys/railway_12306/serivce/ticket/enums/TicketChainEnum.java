package org.zys.railway_12306.serivce.ticket.enums;

/**
 *购票相关责任链枚举
 *
 * @author SUM
 * @date 2026/03/18
 */
public enum TicketChainEnum {

    /**
     * 车票查询过滤器
     */
    TRAIN_QUERY_FILTER,

    /**
     * 车票购买过滤器
     */
    TRAIN_PURCHASE_TICKET_FILTER,

    /**
     * 车票退款过滤器
     */
    TRAIN_REFUND_TICKET_FILTER
}
