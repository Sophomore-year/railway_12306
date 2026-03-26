package org.zys.railway_12306.service.ticket.controller;




import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.web.Results;
import org.zys.railway_12306.service.ticket.pojo.dto.req.CancelTicketOrderReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.PurchaseTicketReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.RefundTicketReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.TicketPageQueryReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.RefundTicketRespDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.TicketPageQueryRespDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.TicketPurchaseRespDTO;
import org.zys.railway_12306.service.ticket.remote.dto.PayInfoRespDTO;
import org.zys.railway_12306.service.ticket.service.TicketService;

/**
 *车票控制层
 *
 * @author SUM
 * @date 2026/03/18
 */
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;


    /**
     * 根据条件查询车票
     * @param requestParam 查询车票请求参数
     */
    @GetMapping("/api/ticket-service/ticket/query")
    public Result<TicketPageQueryRespDTO> pageListTicketQuery(TicketPageQueryReqDTO requestParam) {
        return Results.success(ticketService.pageListTicketQueryV1(requestParam));
    }

    /**
     * 购买车票V1
     */
    @PostMapping("/api/ticket-service/ticket/purchase")
    public Result<TicketPurchaseRespDTO> purchaseTickets(@RequestBody PurchaseTicketReqDTO requestParam) {
        return Results.success(ticketService.purchaseTicketsV1(requestParam));
    }


    /**
     * 取消车票订单
     */
    @PostMapping("/api/ticket-service/ticket/cancel")
    public Result<Void> cancelTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam) {
        ticketService.cancelTicketOrder(requestParam);
        return Results.success();
    }

    /**
     * 支付单详情查询
     */
    @GetMapping("/api/ticket-service/ticket/pay/query")
    public Result<PayInfoRespDTO> getPayInfo(@RequestParam(value = "orderSn") String orderSn) {
        return Results.success(ticketService.getPayInfo(orderSn));
    }

    /**
     * 公共退款接口
     */
    @PostMapping("/api/ticket-service/ticket/refund")
    public Result<RefundTicketRespDTO> commonTicketRefund(@RequestBody RefundTicketReqDTO requestParam) {
        return Results.success(ticketService.commonTicketRefund(requestParam));
    }
}
