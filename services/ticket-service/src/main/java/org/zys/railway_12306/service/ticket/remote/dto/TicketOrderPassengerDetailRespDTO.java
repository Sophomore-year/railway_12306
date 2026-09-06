package org.zys.railway_12306.service.ticket.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车票订单乘客详情返回结果
 *
 * @author SUM
 * @date 2026/03/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketOrderPassengerDetailRespDTO {

    /**
     * 订单明细 ID（部分退款时用于按子订单记录定位）
     */
    private String id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 席别类型
     */
    private Integer seatType;

    /**
     * 车厢号
     */
    private String carriageNumber;

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
     * 车票类型 0：成人 1：儿童 2：学生 3：残疾军人
     */
    private Integer ticketType;

    /**
     * 订单金额
     */
    private Integer amount;

    /**
     * 车票状态
     */
    private Integer status;
}
