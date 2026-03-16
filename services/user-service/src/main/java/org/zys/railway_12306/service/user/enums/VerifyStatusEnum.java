package org.zys.railway_12306.service.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *用户注册错误码枚举
 *
 * @author SUM
 * @date 2026/03/16
 */
@AllArgsConstructor
public enum VerifyStatusEnum {

    /**
     * 未审核
     */
    UNREVIEWED(0),

    /**
     * 已审核
     */
    REVIEWED(1);

    @Getter
    private final int code;
}
