package org.zys.railway_12306.service.ticket.service.handler.ticket.filter.purchase;

import org.zys.rail_12306.framework.starter.designpattern.chain.AbstractChainHandler;
import org.zys.railway_12306.service.ticket.enums.TicketChainEnum;
import org.zys.railway_12306.service.ticket.pojo.dto.req.PurchaseTicketReqDTO;

public interface TrainPurchaseTicketChainFilter <T extends PurchaseTicketReqDTO> extends AbstractChainHandler<PurchaseTicketReqDTO> {

    @Override
    default String mark() {
        return TicketChainEnum.TRAIN_PURCHASE_TICKET_FILTER.name();
    }
}
