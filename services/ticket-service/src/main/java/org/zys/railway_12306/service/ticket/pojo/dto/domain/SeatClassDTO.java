package org.zys.railway_12306.service.ticket.pojo.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *座位类型实体
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatClassDTO {

    /**
     * 座位类型
     */
    private Integer type;

    /**
     * 座位数量
     */
    private Integer quantity;

    /**
     * 座位价格
     */
    private BigDecimal price;

    /**
     * 座位候补标识
     */
    private Boolean candidate;
}
