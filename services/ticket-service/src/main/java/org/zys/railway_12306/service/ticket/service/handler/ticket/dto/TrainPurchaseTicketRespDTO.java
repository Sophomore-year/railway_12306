package org.zys.railway_12306.service.ticket.service.handler.ticket.dto;

import lombok.Data;

/**
 *列车购票出参
 *
 * @author SUM
 * @date 2026/03/22
 */
@Data
public class TrainPurchaseTicketRespDTO {
    /**
     * 乘车人 ID
     */
    private String passengerId;

    /**
     * 乘车人姓名
     */
    private String realName;

    /**
     * 乘车人证件类型
     */
    private Integer idType;

    /**
     * 乘车人证件号
     */
    private String idCard;

    /**
     * 乘车人手机号
     */
    private String phone;

    /**
     * 用户类型 0：成人 1：儿童 2：学生 3：残疾军人
     */
    private Integer userType;

    /**
     * 席别类型
     */
    private Integer seatType;

    /**
     * 车厢号
     */
    private String carriageNumber;

    /**
     * 座位号
     */
    private String seatNumber;

    /**
     * 座位金额
     */
    private Integer amount;

}
