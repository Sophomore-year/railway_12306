package org.zys.railway_12306.service.user.pojo.dto.req;

import lombok.Data;

/**
 *乘车人移除请求参数
 *
 * @author SUM
 * @date 2026/03/11
 */
@Data
public class PassengerRemoveReqDTO {
    /**
     * 乘车人id
     */
    private String id;
}
