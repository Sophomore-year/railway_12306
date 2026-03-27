package org.zys.railway_12306.service.ticket.service;

import org.zys.railway_12306.service.ticket.pojo.dto.domain.SeatTypeCountDTO;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;

import java.util.List;

/**
 *座位接口层
 *
 * @author SUM
 * @date 2026/03/22
 */
public interface SeatService {

    /**
     * 获取列车 startStation 到 endStation 区间可用座位数量
     *
     * @param trainId      列车 ID
     * @param startStation 出发站
     * @param endStation   到达站
     * @param seatTypes    座位类型集合
     * @return 座位剩余可用数量
     */
    List<SeatTypeCountDTO> listSeatTypeCount(Long trainId, String startStation, String endStation, List<Integer> seatTypes);

    /**
     * 锁定选中以及沿途车票状态
     *
     * @param trainId                     列车 ID
     * @param departure                   出发站
     * @param arrival                     到达站
     * @param trainPurchaseTicketRespList 乘车人以及座位信息
     */
    void lockSeat(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespList);

    /**
     * 解锁选中以及沿途车票状态
     *
     * @param trainId                    列车 ID
     * @param departure                  出发站
     * @param arrival                    到达站
     * @param trainPurchaseTicketResults 乘车人以及座位信息
     */
    void unlock(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults);
}
