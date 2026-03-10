package org.zys.rail_12306.framework.starter.distributedid.core.snowflake;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *WorkId 包装器
 *
 * @author SUM
 * @date 2026/03/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkIdWrapper {
    /**
     * 工作ID
     */
    private Long workId;

    /**
     * 数据中心ID
     */
    private Long dataCenterId;
}
