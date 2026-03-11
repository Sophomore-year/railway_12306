package org.zys.railway_12306.service.user.pojo.dto.req;

import lombok.Data;

/**
 *乘车人添加&修改请求参数
 *
 * @author SUM
 * @date 2026/03/11
 */
@Data
public class PassengerReqDTO {
    /**
     * 乘车人id
     */
    private String id;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 证件号码
     */
    private String idCard;

    /**
     * 优惠类型
     */
    private Integer discountType;

    /**
     * 手机号
     */
    private String phone;
}
