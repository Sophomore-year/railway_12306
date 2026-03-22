package org.zys.railway_12306.service.ticket.pojo.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import org.zys.railway_12306.framework.starter.convention.page.PageRequest;

import java.util.Date;

/**
 *车票分页查询请求参数
 *
 * @author SUM
 * @date 2026/03/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TicketPageQueryReqDTO extends PageRequest {
    /**
     * 出发地 Code
     */
    private String fromStation;

    /**
     * 目的地 Code
     */
    private String toStation;

    /**
     * 出发日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date departureDate;

    /**
     * 出发站点
     */
    private String departure;

    /**
     * 到达站点
     */
    private String arrival;
}
