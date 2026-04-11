
package org.zys.railway_12306.service.ticket.service.handler.ticket.filter.query;


import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.service.ticket.pojo.dto.req.TicketPageQueryReqDTO;

/**
 * 查询列车车票流程过滤器之验证数据是否为空或空的字符串
 *
 * @author SUM
 * @date 2026/04/11
 */
@Component
public class TrainTicketQueryParamNotNullChainFilter implements TrainTicketQueryChainFilter<TicketPageQueryReqDTO> {

    @Override
    public void handler(TicketPageQueryReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getFromStation())) {
            throw new ClientException("出发地不能为空");
        }
        if (StrUtil.isBlank(requestParam.getToStation())) {
            throw new ClientException("目的地不能为空");
        }
        if (requestParam.getDepartureDate() == null) {
            throw new ClientException("出发日期不能为空");
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
