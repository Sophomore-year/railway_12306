package org.zys.railway_12306.service.ticket.service.handler.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.PurchaseTicketPassengerDetailDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.PurchaseTicketReqDTO;

import java.util.List;

/**
 *选择座位实体
 *
 * @author SUM
 * @date 2026/03/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  SelectSeatDTO {

    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 座位对应的乘车人集合
     */
    private List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails;

    /**
     * 购票原始入参
     */
    private PurchaseTicketReqDTO requestParam;
}
