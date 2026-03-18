package org.zys.railway_12306.serivce.ticket.enums;


import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *Canal 执行策略标记枚举
 *
 * @author SUM
 * @date 2026/03/18
 */
@RequiredArgsConstructor
public enum CanalExecuteStrategyEnum {

    /**
     * 座位表
     */
    T_SEAT("t_seat", null),

    /**
     * 订单表
     */
    T_ORDER("t_order", "^t_order_([0-9]+|1[0-6])");

    @Getter
    private final String actualTable;

    @Getter
    private final String patternMatchTable;

    public static boolean isPatternMatch(String tableName) {
        return Arrays.stream(CanalExecuteStrategyEnum.values())
                .anyMatch(each -> StrUtil.isNotBlank(each.getPatternMatchTable())
                        && Pattern.compile(each.getPatternMatchTable()).matcher(tableName).matches());
    }

    public static String getPatternMatch(String tableName) {
        return Arrays.stream(CanalExecuteStrategyEnum.values())
                .filter(each -> Objects.equals(tableName, each.getActualTable()))
                .findFirst()
                .map(CanalExecuteStrategyEnum::getPatternMatchTable)
                .orElse(null);
    }
}
