package org.zys.railway_12306.service.ticket.pojo.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *座位类型和座位数量实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatTypeCountDTO {
    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 座位类型 - 对应数量
     */
    private Integer seatCount;
}
