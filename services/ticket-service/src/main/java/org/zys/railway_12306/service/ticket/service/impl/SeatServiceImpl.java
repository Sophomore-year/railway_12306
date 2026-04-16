package org.zys.railway_12306.service.ticket.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.railway_12306.service.ticket.enums.SeatStatusEnum;
import org.zys.railway_12306.service.ticket.mapper.SeatMapper;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.RouteDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.SeatTypeCountDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.Seat;
import org.zys.railway_12306.service.ticket.service.SeatService;
import org.zys.railway_12306.service.ticket.service.TrainStationService;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.TRAIN_STATION_CARRIAGE_REMAINING_TICKET;

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
    private final TrainStationService trainStationService;
    private final DistributedCache distributedCache;

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

    /**
     * 锁定座位
     * <p>
     * 该方法用于在购票成功后锁定座位，防止重复售票。
     * 核心逻辑：
     * 1. 获取列车站点间的所有区间路由
     * 2. 对每个座位的每个区间进行锁定
     * 3. 更新座位状态为锁定状态
     * </p>
     * @param trainId 列车ID
     * @param departure 出发站
     * @param arrival 到达站
     * @param trainPurchaseTicketRespList 购票结果列表
     */
    @Override
    public void lockSeat(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespList) {
        // 1. 获取列车站点间的所有区间路由
        // 例如：北京->上海的列车，可能经过济南、南京等站点，需要锁定北京->济南、济南->南京、南京->上海等区间的座位
        List<RouteDTO> routeList = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);

        // 2. 遍历每个购票结果和每个区间路由，锁定对应的座位
        trainPurchaseTicketRespList.forEach(each -> routeList.forEach(item -> {
            // 2.1 构建更新条件
            LambdaUpdateWrapper<Seat> updateWrapper = Wrappers.lambdaUpdate(Seat.class)
                    .eq(Seat::getTrainId, trainId)  // 列车ID
                    .eq(Seat::getCarriageNumber, each.getCarriageNumber())  // 车厢号
                    .eq(Seat::getStartStation, item.getStartStation())  // 区间起点站
                    .eq(Seat::getEndStation, item.getEndStation())  // 区间终点站
                    .eq(Seat::getSeatNumber, each.getSeatNumber());  // 座位号

            // 2.2 构建更新对象，设置座位状态为锁定
            Seat updateSeatDO = Seat.builder()
                    .seatStatus(SeatStatusEnum.LOCKED.getCode())
                    .build();

            // 2.3 执行更新操作
            seatMapper.update(updateSeatDO, updateWrapper);
        }));
    }

    @Override
    public void unlock(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults) {
        List<RouteDTO> routeList = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
        trainPurchaseTicketResults.forEach(each -> routeList.forEach(item -> {
            LambdaUpdateWrapper<Seat> updateWrapper = Wrappers.lambdaUpdate(Seat.class)
                    .eq(Seat::getTrainId, trainId)
                    .eq(Seat::getCarriageNumber, each.getCarriageNumber())
                    .eq(Seat::getStartStation, item.getStartStation())
                    .eq(Seat::getEndStation, item.getEndStation())
                    .eq(Seat::getSeatNumber, each.getSeatNumber());
            Seat updateSeat = Seat.builder()
                    .seatStatus(SeatStatusEnum.AVAILABLE.getCode())
                    .build();
            seatMapper.update(updateSeat, updateWrapper);
        }));
    }

    @Override
    public List<String> listUsableCarriageNumber(String trainId, Integer carriageType, String departure, String arrival) {
        LambdaQueryWrapper<Seat> queryWrapper = Wrappers.lambdaQuery(Seat.class)
                .eq(Seat::getTrainId, trainId)
                .eq(Seat::getSeatType, carriageType)
                .eq(Seat::getStartStation, departure)
                .eq(Seat::getEndStation, arrival)
                .eq(Seat::getSeatStatus, SeatStatusEnum.AVAILABLE.getCode())
                .groupBy(Seat::getCarriageNumber)
                .select(Seat::getCarriageNumber);
        List<Seat> seatList = seatMapper.selectList(queryWrapper);
        return seatList.stream().map(Seat::getCarriageNumber).collect(Collectors.toList());
    }

    @Override
    public List<Integer> listSeatRemainingTicket(String trainId, String departure, String arrival, List<String> trainCarriageList) {
        String keySuffix = StrUtil.join("_", trainId, departure, arrival);
        if (distributedCache.hasKey(TRAIN_STATION_CARRIAGE_REMAINING_TICKET + keySuffix)) {
            StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
            List<Object> trainStationCarriageRemainingTicket =
                    stringRedisTemplate.opsForHash().multiGet(TRAIN_STATION_CARRIAGE_REMAINING_TICKET + keySuffix, Arrays.asList(trainCarriageList.toArray()));
            if (CollUtil.isNotEmpty(trainStationCarriageRemainingTicket)) {
                return trainStationCarriageRemainingTicket.stream().map(each -> Integer.parseInt(each.toString())).collect(Collectors.toList());
            }
        }
        Seat seat = Seat.builder()
                .trainId(Long.parseLong(trainId))
                .startStation(departure)
                .endStation(arrival)
                .build();
        return seatMapper.listSeatRemainingTicket(seat, trainCarriageList);
    }

    @Override
    public List<String> listAvailableSeat(String trainId, String carriageNumber, Integer seatType, String departure, String arrival) {
        LambdaQueryWrapper<Seat> queryWrapper = Wrappers.lambdaQuery(Seat.class)
                .eq(Seat::getTrainId, trainId)
                .eq(Seat::getCarriageNumber, carriageNumber)
                .eq(Seat::getSeatType, seatType)
                .eq(Seat::getStartStation, departure)
                .eq(Seat::getEndStation, arrival)
                .eq(Seat::getSeatStatus, SeatStatusEnum.AVAILABLE.getCode())
                .select(Seat::getSeatNumber);
        List<Seat> seatList = seatMapper.selectList(queryWrapper);
        return seatList.stream().map(Seat::getSeatNumber).collect(Collectors.toList());
    }
}
