package org.zys.railway_12306.framework.starter.common.enums;

/**
 * 操作类型枚举
 *
 * @author SUM
 * @date 2026/03/16
 */
public enum StatusEnum {
    /**
     * 成功
     */
    SUCCESS(0),

    /**
     * 失败
     */
    FAIL(1);

    private final Integer statusCode;

    StatusEnum(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer code() {
        return this.statusCode;
    }

    public String strCode() {
        return String.valueOf(this.statusCode);
    }

    @Override
    public String toString() {
        return strCode();
    }
}
