package org.zys.railway_12306.framework.starter.common.enums;

/**
 *标识枚举，非 {@link Boolean#TRUE} 即 {@link Boolean#FALSE}
 *
 * @author SUM
 * @date 2026/03/16
 */
public enum FlagEnum {
    /**
     * FALSE
     */
    FALSE(0),

    /**
     * TRUE
     */
    TRUE(1);

    private final Integer flag;

    FlagEnum(Integer flag) {
        this.flag = flag;
    }

    public Integer code() {
        return this.flag;
    }

    public String strCode() {
        return String.valueOf(this.flag);
    }

    @Override
    public String toString() {
        return strCode();
    }
}
