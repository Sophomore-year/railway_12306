package org.zys.railway_12306.service.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.SeatTypeCountDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.Seat;

import java.util.List;

public interface SeatMapper extends BaseMapper<Seat> {

    /**
     * 获取列车车厢余票集合
     */
    List<Integer> listSeatRemainingTicket(@Param("seat") Seat seat, @Param("trainCarriageList") List<String> trainCarriageList);


    /**
     * 获取列车 startStation 到 endStation 区间可用座位数量
     */
    List<SeatTypeCountDTO> listSeatTypeCount(@Param("trainId") Long trainId, @Param("startStation") String startStation, @Param("endStation") String endStation, @Param("seatTypes") List<Integer> seatTypes);
}
