package org.zys.railway_12306.serivce.ticket.pojo.dto.resp;

import lombok.Data;

/**
 *站点分页查询响应参数
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
public class StationQueryRespDTO {
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

    /**
     * 城市名称
     */
    private String regionName;
}
