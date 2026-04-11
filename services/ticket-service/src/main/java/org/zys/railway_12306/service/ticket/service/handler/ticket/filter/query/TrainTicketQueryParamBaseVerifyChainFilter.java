package org.zys.railway_12306.service.ticket.service.handler.ticket.filter.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.service.ticket.pojo.dto.req.TicketPageQueryReqDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 查询列车车票流程过滤器之基础数据验证
 *
 * @author SUM
 * @date 2026/04/11
 */
@Component
@RequiredArgsConstructor
public class TrainTicketQueryParamBaseVerifyChainFilter implements TrainTicketQueryChainFilter<TicketPageQueryReqDTO> {

    @Override
    public void handler(TicketPageQueryReqDTO requestParam) {
        if (requestParam.getDepartureDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(LocalDate.now())) {
            throw new ClientException("出发日期不能小于当前日期");
        }
        if (Objects.equals(requestParam.getFromStation(), requestParam.getToStation())) {
            throw new ClientException("出发地和目的地不能相同");
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
