package org.zys.railway_12306.service.pay.pojo.dto;

import lombok.Data;
import org.zys.railway_12306.service.pay.enums.RefundTypeEnum;
import org.zys.railway_12306.service.pay.remote.dto.TicketOrderPassengerDetailRespDTO;

import java.util.List;

/**
 *退款请求入参数实体
 *
 * @author SUM
 * @date 2026/03/29
 */
@Data
public class RefundReqDTO {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 退款类型枚举
     */
    private RefundTypeEnum refundTypeEnum;

    /**
     * 退款金额
     */
    private Integer refundAmount;

    /**
     * 部分退款车票详情集合
     */
    private List<TicketOrderPassengerDetailRespDTO> refundDetailReqDTOList;
}
