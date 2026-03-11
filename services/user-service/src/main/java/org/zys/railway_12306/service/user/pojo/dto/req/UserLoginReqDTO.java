package org.zys.railway_12306.service.user.pojo.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *用户登录请求参数
 *
 * @author SUM
 * @date 2026/03/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginReqDTO {
    /**
     * 用户名
     */
    private String usernameOrMailOrPhone;

    /**
     * 密码
     */
    private String password;
}
