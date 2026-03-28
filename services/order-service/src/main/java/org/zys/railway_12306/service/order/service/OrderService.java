package org.zys.railway_12306.service.order.service;

import org.zys.railway_12306.framework.starter.convention.page.PageResponse;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderCreateReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderPageQueryReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.req.TicketOrderSelfPageQueryReqDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderDetailRespDTO;
import org.zys.railway_12306.service.order.pojo.dto.resp.TicketOrderDetailSelfRespDTO;

/**
 *订单接口层
 *
 * @author SUM
 * @date 2026/03/28
 */
public interface OrderService {

    /**
     * 根据订单号查询车票订单
     *
     * @param orderSn 订单号
     * @return {@link TicketOrderDetailRespDTO}
     */
    TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn);

    /**
     * 分页查询车票订单
     *
     * @param requestParam 请求参数
     * @return {@link PageResponse<TicketOrderDetailRespDTO>} 订单分页详情
     */
    PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam);

    /**
     * 分页查询本人车票订单
     *
     * @param requestParam 请求参数
     * @return {@link PageResponse<TicketOrderDetailSelfRespDTO>} 本人车票订单集合
     */
    PageResponse<TicketOrderDetailSelfRespDTO> pageSelfTicketOrder(TicketOrderSelfPageQueryReqDTO requestParam);

    /**
     * 创建车票订单
     *
     * @param requestParam 创建车票订单请求参数
     * @return 订单号
     */
    String createTicketOrder(TicketOrderCreateReqDTO requestParam);
}
