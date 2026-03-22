package org.zys.railway_12306.service.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.service.ticket.mapper.TrainStationMapper;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.RouteDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.TrainStationQueryRespDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.TrainStation;
import org.zys.railway_12306.service.ticket.service.TrainStationService;
import org.zys.railway_12306.service.ticket.toolkit.StationCalculateUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 *列车站点接口实现层
 *
 * @author SUM
 * @date 2026/03/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainStationServiceImpl implements TrainStationService {

    private final TrainStationMapper trainStationMapper;

    /**
     * 查询列车站点信息
     * @param trainId 列车ID
     * @return 列车站点列表
     */
    @Override
    public List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId) {
        // 构建查询条件，查询指定列车的所有站点
        LambdaQueryWrapper<TrainStation> queryWrapper = Wrappers.lambdaQuery(TrainStation.class)
                .eq(TrainStation::getTrainId, trainId);
        // 执行查询
        List<TrainStation> trainStationList = trainStationMapper.selectList(queryWrapper);
        // 将查询结果转换为DTO并返回
        return BeanUtil.convert(trainStationList, TrainStationQueryRespDTO.class);
    }

    /**
     * 查询列车站点路线（途经站点）
     * @param trainId 列车ID
     * @param departure 出发站
     * @param arrival 到达站
     * @return 路线列表
     */
    @Override
    public List<RouteDTO> listTrainStationRoute(String trainId, String departure, String arrival) {
        // 构建查询条件，查询指定列车的所有站点名称
        LambdaQueryWrapper<TrainStation> queryWrapper = Wrappers.lambdaQuery(TrainStation.class)
                .eq(TrainStation::getTrainId, trainId)
                .select(TrainStation::getDeparture);
        // 执行查询
        List<TrainStation> trainStationList = trainStationMapper.selectList(queryWrapper);
        // 提取站点名称列表
        List<String> trainStationAllList = trainStationList.stream().map(TrainStation::getDeparture).collect(Collectors.toList());
        // 计算途经站点
        return StationCalculateUtil.throughStation(trainStationAllList, departure, arrival);
    }

    /**
     * 查询列车站点路线（反向途经站点）
     * @param trainId 列车ID
     * @param departure 出发站
     * @param arrival 到达站
     * @return 路线列表
     */
    @Override
    public List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival) {
        // 构建查询条件，查询指定列车的所有站点名称
        LambdaQueryWrapper<TrainStation> queryWrapper = Wrappers.lambdaQuery(TrainStation.class)
                .eq(TrainStation::getTrainId, trainId)
                .select(TrainStation::getDeparture);
        // 执行查询
        List<TrainStation> trainStationList = trainStationMapper.selectList(queryWrapper);
        // 提取站点名称列表
        List<String> trainStationAllList = trainStationList.stream().map(TrainStation::getDeparture).collect(Collectors.toList());
        // 计算反向途经站点
        return StationCalculateUtil.takeoutStation(trainStationAllList, departure, arrival);
    }

}
