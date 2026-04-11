package org.zys.railway_12306.service.ticket.service.handler.ticket.filter.query;

import org.zys.rail_12306.framework.starter.designpattern.chain.AbstractChainHandler;
import org.zys.railway_12306.service.ticket.enums.TicketChainEnum;
import org.zys.railway_12306.service.ticket.pojo.dto.req.TicketPageQueryReqDTO;

public interface TrainTicketQueryChainFilter<T extends TicketPageQueryReqDTO> extends AbstractChainHandler<TicketPageQueryReqDTO> {
    @Override
    default String mark() {
        return TicketChainEnum.TRAIN_QUERY_FILTER.name();
    }
}
