package org.zys.railway_12306.service.ticket.service.handler.ticket.select;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zys.rail_12306.framework.starter.designpattern.strategy.AbstractStrategyChoose;
import org.zys.railway_12306.framework.starter.convention.exception.RemoteException;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.user.core.UserContext;
import org.zys.railway_12306.service.ticket.enums.VehicleSeatTypeEnum;
import org.zys.railway_12306.service.ticket.enums.VehicleTypeEnum;
import org.zys.railway_12306.service.ticket.mapper.TrainStationPriceMapper;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.PurchaseTicketPassengerDetailDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.PurchaseTicketReqDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.TrainStationPrice;
import org.zys.railway_12306.service.ticket.remote.UserRemoteService;
import org.zys.railway_12306.service.ticket.remote.dto.PassengerRespDTO;
import org.zys.railway_12306.service.ticket.service.SeatService;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.SelectSeatDTO;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 *购票时列车座位选择器
 *
 * @author SUM
 * @date 2026/03/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainSeatTypeSelector {


    private final SeatService seatService;
    private final UserRemoteService userRemoteService;
    private final TrainStationPriceMapper trainStationPriceMapper;
    private final AbstractStrategyChoose abstractStrategyChoose;
    private final ThreadPoolExecutor selectSeatThreadPoolExecutor;


    /**
     * 选择座位并分配车票
     * <p>
     * 该方法是座位选择的核心实现，主要完成以下步骤：
     * 1. 按座位类型分组乘客信息
     * 2. 并行或串行分配座位
     * 3. 验证座位分配结果
     * 4. 远程查询乘客详细信息
     * 5. 计算票价并填充乘客信息
     * 6. 锁定座位
     * </p>
     * @param trainType 列车类型
     * @param requestParam 购票请求参数
     * @return 座位分配结果列表
     */
    public List<TrainPurchaseTicketRespDTO> select(Integer trainType, PurchaseTicketReqDTO requestParam) {
        // 1. 获取乘客详情列表
        List<PurchaseTicketPassengerDetailDTO> passengerDetails = requestParam.getPassengers();
        
        // 2. 按座位类型分组乘客信息
        Map<Integer, List<PurchaseTicketPassengerDetailDTO>> seatTypeMap = passengerDetails.stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
        
        // 3. 初始化结果列表（使用线程安全的集合）
        List<TrainPurchaseTicketRespDTO> actualResult = Collections.synchronizedList(new ArrayList<>(seatTypeMap.size()));
        
        // 4. 根据座位类型数量选择不同的处理方式
        if (seatTypeMap.size() > 1) {
            // 4.1 多座位类型，使用线程池并行处理
            List<Future<List<TrainPurchaseTicketRespDTO>>> futureResults = new ArrayList<>(seatTypeMap.size());
            seatTypeMap.forEach((seatType, passengerSeatDetails) -> {
                // 提交任务到线程池
                Future<List<TrainPurchaseTicketRespDTO>> completableFuture = selectSeatThreadPoolExecutor
                        .submit(() -> distributeSeats(trainType, seatType, requestParam, passengerSeatDetails));
                futureResults.add(completableFuture);
            });
            
            // 4.2 收集并行处理结果
            futureResults.parallelStream().forEach(completableFuture -> {
                try {
                    actualResult.addAll(completableFuture.get());
                } catch (Exception e) {
                    throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
                }
            });
        } else {
            // 4.3 单座位类型，直接处理
            seatTypeMap.forEach((seatType, passengerSeatDetails) -> {
                List<TrainPurchaseTicketRespDTO> aggregationResult = distributeSeats(trainType, seatType, requestParam, passengerSeatDetails);
                actualResult.addAll(aggregationResult);
            });
        }
        
        // 5. 验证座位分配结果
        if (CollUtil.isEmpty(actualResult) || !Objects.equals(actualResult.size(), passengerDetails.size())) {
            throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
        }
        
        // 6. 提取乘客ID列表
        List<String> passengerIds = actualResult.stream()
                .map(TrainPurchaseTicketRespDTO::getPassengerId)
                .collect(Collectors.toList());
        
        // 7. 远程查询乘客详细信息
        Result<List<PassengerRespDTO>> passengerRemoteResult;
        List<PassengerRespDTO> passengerRemoteResultList;
        try {
            passengerRemoteResult = userRemoteService.listPassengerQueryByIds(UserContext.getUsername(), passengerIds);
            if (!passengerRemoteResult.isSuccess() || CollUtil.isEmpty(passengerRemoteResultList = passengerRemoteResult.getData())) {
                throw new RemoteException("用户服务远程调用查询乘车人相关信息错误");
            }
        } catch (Throwable ex) {
            if (ex instanceof RemoteException) {
                log.error("用户服务远程调用查询乘车人相关信息错误，当前用户：{}，请求参数：{}", UserContext.getUsername(), passengerIds);
            } else {
                log.error("用户服务远程调用查询乘车人相关信息错误，当前用户：{}，请求参数：{}", UserContext.getUsername(), passengerIds, ex);
            }
            throw ex;
        }
        
        // 8. 填充乘客信息和计算票价
        actualResult.forEach(each -> {
            String passengerId = each.getPassengerId();
            // 8.1 填充乘客详细信息
            passengerRemoteResultList.stream()
                    .filter(item -> Objects.equals(item.getId(), passengerId))
                    .findFirst()
                    .ifPresent(passenger -> {
                        each.setIdCard(passenger.getIdCard());
                        each.setPhone(passenger.getPhone());
                        each.setUserType(passenger.getDiscountType());
                        each.setIdType(passenger.getIdType());
                        each.setRealName(passenger.getRealName());
                    });
            
            // 8.2 查询并设置票价
            LambdaQueryWrapper<TrainStationPrice> lambdaQueryWrapper = Wrappers.lambdaQuery(TrainStationPrice.class)
                    .eq(TrainStationPrice::getTrainId, requestParam.getTrainId())
                    .eq(TrainStationPrice::getDeparture, requestParam.getDeparture())
                    .eq(TrainStationPrice::getArrival, requestParam.getArrival())
                    .eq(TrainStationPrice::getSeatType, each.getSeatType())
                    .select(TrainStationPrice::getPrice);
            TrainStationPrice trainStationPrice = trainStationPriceMapper.selectOne(lambdaQueryWrapper);
            each.setAmount(trainStationPrice.getPrice());
        });
        
        // 9. 锁定座位
        seatService.lockSeat(requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival(), actualResult);
        
        // 10. 返回座位分配结果
        return actualResult;
    }

    /**
     * 分配座位
     * <p>
     * 根据列车类型和座位类型选择对应的座位分配策略
     * </p>
     * @param trainType 列车类型
     * @param seatType 座位类型
     * @param requestParam 购票请求参数
     * @param passengerSeatDetails 乘客座位详情
     * @return 座位分配结果列表
     */
    private List<TrainPurchaseTicketRespDTO> distributeSeats(Integer trainType, Integer seatType, PurchaseTicketReqDTO requestParam, List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails) {
        // 1. 构建策略键，格式为：列车类型+座位类型
        String buildStrategyKey = VehicleTypeEnum.findNameByCode(trainType) + VehicleSeatTypeEnum.findNameByCode(seatType);
        
        // 2. 构建座位选择DTO
        SelectSeatDTO selectSeatDTO = SelectSeatDTO.builder()
                .seatType(seatType)
                .passengerSeatDetails(passengerSeatDetails)
                .requestParam(requestParam)
                .build();
        
        // 3. 根据策略键选择并执行对应的座位分配策略
        try {
            return abstractStrategyChoose.chooseAndExecuteResp(buildStrategyKey, selectSeatDTO);
        } catch (ServiceException ex) {
            throw new ServiceException("当前车次列车类型暂未适配，请购买G35或G39车次");
        }
    }
}
