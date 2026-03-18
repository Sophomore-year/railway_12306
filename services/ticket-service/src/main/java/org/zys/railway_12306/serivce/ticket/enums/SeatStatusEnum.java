package org.zys.railway_12306.serivce.ticket.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *座位状态枚举
 *
 * @author SUM
 * @date 2026/03/18
 */
@RequiredArgsConstructor
public enum SeatStatusEnum {
    /**
     * 可售
     */
    AVAILABLE(0),

    /**
     * 锁定
     */
    LOCKED(1),

    /**
     * 已售
     */
    SOLD(2);

    @Getter
    private final Integer code;
}
