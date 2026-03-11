package org.zys.railway_12306.service.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.zys.rail_12306.framework.starter.database.base.BaseDO;

/**
 *用户名复用表实体
 *
 * @author SUM
 * @date 2026/03/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_user_reuse")
public class UserReuse extends BaseDO {
}
