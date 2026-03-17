package org.zys.railway_12306.service.gateway.toolkit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *用户信息实体
 *
 * @author SUM
 * @date 2026/03/17
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDTO {

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;
}
