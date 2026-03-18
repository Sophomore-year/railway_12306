package org.zys.railway_12306.serivce.ticket.pojo.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *站点路线实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {
    /**
     * 出发站点
     */
    private String startStation;

    /**
     * 目的站点
     */
    private String endStation;
}
