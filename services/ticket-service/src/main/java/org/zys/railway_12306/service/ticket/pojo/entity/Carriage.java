package org.zys.railway_12306.service.ticket.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.rail_12306.framework.starter.database.base.BaseDO;

/**
 *车厢实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_carriage")
public class Carriage extends BaseDO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 列车id
     */
    private Long trainId;

    /**
     * 车厢号
     */
    private String carriageNumber;

    /**
     * 车厢类型
     */
    private Integer carriageType;

    /**
     * 座位数
     */
    private Integer seatCount;
}
