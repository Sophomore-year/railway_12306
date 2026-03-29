package org.zys.railway_12306.service.pay.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *退款类型枚举
 *
 * @author SUM
 * @date 2026/03/29
 */
@Getter
@RequiredArgsConstructor
public enum RefundTypeEnum {

    /**
     * 部分退款
     */
    PARTIAL_REFUND(11, 0, "PARTIAL_REFUND", "部分退款"),

    /**
     * 全部退款
     */
    FULL_REFUND(12, 1, "FULL_REFUND", "全部退款");

    private final Integer code;

    private final Integer type;

    private final String name;

    private final String value;
}
