package org.zys.rail_12306.framework.starter.idempotent.core.token;

import org.zys.rail_12306.framework.starter.idempotent.core.IdempotentExecuteHandler;

/**
 *
 *Token 实现幂等接口
 * @author SUM
 * @date 2026/03/10
 */
public interface IdempotentTokenService extends IdempotentExecuteHandler {
    /**
     * 创建幂等验证Token
     */
    String createToken();
}
