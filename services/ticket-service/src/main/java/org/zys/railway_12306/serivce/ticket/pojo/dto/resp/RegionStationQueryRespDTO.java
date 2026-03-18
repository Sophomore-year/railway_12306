package org.zys.railway_12306.serivce.ticket.pojo.dto.resp;

import lombok.Data;

/**
 *地区&站点分页查询响应参数
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
public class RegionStationQueryRespDTO {
    /**
     * 名称
     */
    private String name;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 拼音
     */
    private String spell;
}
