package org.zys.railway_12306.service.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestBody;
import org.zys.railway_12306.service.ticket.pojo.dto.req.CancelTicketOrderReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.PurchaseTicketReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.RefundTicketReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.TicketPageQueryReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.RefundTicketRespDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.TicketPageQueryRespDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.TicketPurchaseRespDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.Ticket;
import org.zys.railway_12306.service.ticket.remote.dto.PayInfoRespDTO;

/**
 *车票接口
 *
 * @author SUM
 * @date 2026/03/22
 */
public interface TicketService extends IService<Ticket> {

    /**
     * 根据条件分页查询车票
     *
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam);


    /**
     * 购买车票
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    TicketPurchaseRespDTO purchaseTicketsV1(@RequestBody PurchaseTicketReqDTO requestParam);

    /**
     * 执行购买车票
     * 被对应购票版本号接口调用 {@link TicketService#purchaseTicketsV1(PurchaseTicketReqDTO)}
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    TicketPurchaseRespDTO executePurchaseTickets(@RequestBody PurchaseTicketReqDTO requestParam);

    /**
     * 取消车票订单
     *
     * @param requestParam 取消车票订单入参
     */
    void cancelTicketOrder(CancelTicketOrderReqDTO requestParam);

    /**
     * 释放订单占用的座位并恢复余票缓存
     * <p>
     * 供用户主动取消订单、延时自动关单等场景调用，内部通过订单维度分布式锁保证幂等，
     * 同一订单的座位释放只会执行一次。
     * </p>
     *
     * @param orderSn 订单号
     */
    void releaseSeatResources(String orderSn);

    /**
     * 支付单详情查询
     *
     * @param orderSn 订单号
     * @return 支付单详情
     */
    PayInfoRespDTO getPayInfo(String orderSn);

    /**
     * 公共退款接口
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam);
}
