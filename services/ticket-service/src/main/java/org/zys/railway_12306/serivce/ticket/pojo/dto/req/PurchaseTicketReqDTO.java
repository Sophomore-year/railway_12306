package org.zys.railway_12306.serivce.ticket.pojo.dto.req;

import lombok.Data;
import org.zys.railway_12306.serivce.ticket.pojo.dto.domain.PurchaseTicketPassengerDetailDTO;

import java.util.List;

/**
 *购票请求入参
 *
 * @author SUM
 * @date 2026/03/18
 */
@Data
public class PurchaseTicketReqDTO {
    /**
     * 车次 ID
     */
    private String trainId;

    /**
     * 乘车人
     */
    private List<PurchaseTicketPassengerDetailDTO> passengers;

    /**
     * 选择座位
     */
    private List<String> chooseSeats;

    /**
     * 出发站点
     */
    private String departure;

    /**
     * 到达站点
     */
    private String arrival;
}
