package org.zys.railway_12306.serivce.ticket.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.zys.rail_12306.framework.starter.database.base.BaseDO;

/**
 *列车实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_train")
public class Train extends BaseDO {
}
