package org.zys.railway_12306.service.ticket.service.handler.ticket.filter.purchase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.zys.railway_12306.service.ticket.pojo.dto.req.PurchaseTicketReqDTO;


/**
 *购票流程过滤器之验证乘客是否重复购买
 *
 * @author SUM
 * @date 2026/04/17
 */
@Component
@RequiredArgsConstructor
public class TrainPurchaseTicketRepeatChainHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO>{
    @Override
    public void handler(PurchaseTicketReqDTO requestParam) {
        // TODO 重复购买验证后续实现
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
