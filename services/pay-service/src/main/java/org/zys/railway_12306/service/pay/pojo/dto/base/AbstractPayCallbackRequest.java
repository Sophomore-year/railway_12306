package org.zys.railway_12306.service.pay.pojo.dto.base;

import lombok.Getter;
import lombok.Setter;

/**
 *抽象支付回调入参实体
 *
 * @author SUM
 * @date 2026/03/29
 */
public abstract class AbstractPayCallbackRequest implements PayCallbackRequest {

    @Getter
    @Setter
    private String orderRequestId;

    @Override
    public AliPayCallbackRequest getAliPayCallBackRequest() {
        return null;
    }

    @Override
    public String buildMark() {
        return null;
    }
}
