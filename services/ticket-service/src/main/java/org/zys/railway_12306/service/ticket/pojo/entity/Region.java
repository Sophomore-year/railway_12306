package org.zys.railway_12306.service.ticket.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.rail_12306.framework.starter.database.base.BaseDO;


/**
 *地区表
 *
 * @author SUM
 * @date 2026/03/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_region")
public class Region extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 地区全名
     */
    private String fullName;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 地区首字母
     */
    private String initial;

    /**
     * 拼音
     */
    private String spell;

    /**
     * 热门标识
     */
    private Integer popularFlag;
}
