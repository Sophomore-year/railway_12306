package org.zys.railway_12306.serivce.ticket.pojo.dto.req;

import lombok.Data;

import java.util.List;

/**
 *车票退款请求入参数实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
public class RefundTicketReqDTO {
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 退款类型 0 部分退款 1 全部退款
     */
    private Integer type;

    /**
     * 部分退款子订单记录id集合
     */
    private List<String> subOrderRecordIdReqList;
}
