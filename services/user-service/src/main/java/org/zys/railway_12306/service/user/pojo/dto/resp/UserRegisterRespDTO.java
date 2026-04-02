package org.zys.railway_12306.service.user.pojo.dto.resp;

import lombok.Data;

/**
 *用户注册返回参数
 *
 * @author SUM
 * @date 2026/03/11
 */
@Data
public class UserRegisterRespDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;
}
