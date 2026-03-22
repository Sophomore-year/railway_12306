package org.zys.railway_12306.service.ticket.pojo.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *地区&站点查询请求入参
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegionStationQueryReqDTO {

    /**
     * 查询方式
     */
    private Integer queryType;

    /**
     * 名称
     */
    private String name;
}
