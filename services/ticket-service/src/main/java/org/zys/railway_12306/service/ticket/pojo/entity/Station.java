package org.zys.railway_12306.service.ticket.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.rail_12306.framework.starter.database.base.BaseDO;

/**
 *车站实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_station")
public class Station extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 车站编码
     */
    private String code;

    /**
     * 车站名称
     */
    private String name;

    /**
     * 拼音
     */
    private String spell;

    /**
     * 地区编号
     */
    private String region;

    /**
     * 地区名称
     */
    private String regionName;
}
