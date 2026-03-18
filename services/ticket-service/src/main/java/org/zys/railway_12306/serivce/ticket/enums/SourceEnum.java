package org.zys.railway_12306.serivce.ticket.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 *购票来源
 *
 * @author SUM
 * @date 2026/03/18
 */
@RequiredArgsConstructor
public enum SourceEnum {
    /**
     * 互联网购票
     */
    INTERNET(0),

    /**
     * 线下窗口购票
     */
    OFFLINE(1);

    @Getter
    private final Integer code;
}
