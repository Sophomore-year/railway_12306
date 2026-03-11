package org.zys.railway_12306.service.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zys.rail_12306.framework.starter.database.base.BaseDO;

import java.util.Date;

/**
 * 乘客表实体对象
 *
 * @author SUM
 * @date 2026/03/11
 */
@Data
@TableName("t_passenger")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 证件号码
     */
    private String idCard;

    /**
     * 优惠类型
     */
    private Integer discountType;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 添加日期
     */
    private Date createDate;

    /**
     * 审核状态
     */
    private Integer verifyStatus;
}
