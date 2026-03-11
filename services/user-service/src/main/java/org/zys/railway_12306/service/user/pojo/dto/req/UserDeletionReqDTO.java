package org.zys.railway_12306.service.user.pojo.dto.req;

import lombok.Data;

/**
 *用户注销请求参数
 *
 * @author SUM
 * @date 2026/03/11
 */
@Data
public class UserDeletionReqDTO {
    /**
     * 用户名
     */
    private String username;
}
