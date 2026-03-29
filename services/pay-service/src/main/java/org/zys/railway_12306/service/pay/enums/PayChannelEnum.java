package org.zys.railway_12306.service.pay.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *支付渠道枚举
 *
 * @author SUM
 * @date 2026/03/29
 */
@RequiredArgsConstructor
public enum PayChannelEnum {

    /**
     * 支付宝
     */
    ALI_PAY(0, "ALI_PAY", "支付宝");

    @Getter
    private final Integer code;

    @Getter
    private final String name;

    @Getter
    private final String value;
}
