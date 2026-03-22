package org.zys.railway_12306.service.ticket.pojo.dto.domain;

import lombok.Data;

/**
 *购票乘车人详情实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
public class PurchaseTicketPassengerDetailDTO {

    /**
     * 乘车人 ID
     */
    private String passengerId;

    /**
     * 座位类型
     */
    private Integer seatType;
}
