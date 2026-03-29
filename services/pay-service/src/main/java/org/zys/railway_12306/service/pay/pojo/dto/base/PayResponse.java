package org.zys.railway_12306.service.pay.pojo.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *支付返回
 *
 * @author SUM
 * @date 2026/03/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class PayResponse {

    /**
     * 调用支付返回信息
     */
    private String body;
}
