package org.zys.railway_12306.service.pay.pojo.dto;

import lombok.Data;
import org.zys.railway_12306.service.pay.remote.dto.TicketOrderPassengerDetailRespDTO;

import java.util.List;


/**
 *退款创建入参数实体
 *
 * @author SUM
 * @date 2026/05/30
 */
@Data
public class RefundCreateDTO {

    /**
     * 支付流水号
     */
    private String paySn;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 退款类型
     */
    private Integer type;

    /**
     * 部分退款车票详情集合
     */
    private List<TicketOrderPassengerDetailRespDTO> refundDetailReqDTOList;
}
