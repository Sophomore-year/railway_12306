package org.zys.railway_12306.service.order.pojo.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.railway_12306.framework.starter.convention.page.PageRequest;

/**
 *车票订单分页查询
 *
 * @author SUM
 * @date 2026/03/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TicketOrderPageQueryReqDTO extends PageRequest {
    /**
     * 用户唯一标识
     */
    private String userId;

    /**
     * 状态类型 0：未完成 1：未出行 2：历史订单
     */
    private Integer statusType;
}
