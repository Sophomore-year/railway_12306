package org.zys.railway_12306.service.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zys.railway_12306.service.ticket.mapper.SeatMapper;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.SeatTypeCountDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.Seat;
import org.zys.railway_12306.service.ticket.service.SeatService;

import java.util.List;

/**
 *座位接口层实现
 *
 * @author SUM
 * @date 2026/03/22
 */
@Service
@RequiredArgsConstructor
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {
    private final SeatMapper seatMapper;

    /**
     * 查询列车座位类型和数量
     * @param trainId 列车ID
     * @param startStation 出发站
     * @param endStation 到达站
     * @param seatTypes 座位类型列表
     * @return 座位类型和数量列表
     */
    @Override
    public List<SeatTypeCountDTO> listSeatTypeCount(Long trainId, String startStation, String endStation, List<Integer> seatTypes) {
        // 调用Mapper层方法查询座位类型和数量
        return seatMapper.listSeatTypeCount(trainId, startStation, endStation, seatTypes);
    }
}
