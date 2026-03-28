package org.zys.railway_12306.service.order.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.zys.rail_12306.framework.starter.database.base.BaseDO;

/**
 *订单明细数据库实体
 *
 * @author SUM
 * @date 2026/03/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@TableName("t_order_item")
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 列车id
     */
    private Long trainId;

    /**
     * 车厢号
     */
    private String carriageNumber;

    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 座位号
     */
    private String seatNumber;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 证件号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 订单金额
     */
    private Integer amount;

    /**
     * 车票类型
     */
    private Integer ticketType;
}
