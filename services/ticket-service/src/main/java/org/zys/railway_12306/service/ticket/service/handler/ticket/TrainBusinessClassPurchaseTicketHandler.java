package org.zys.railway_12306.service.ticket.service.handler.ticket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.service.ticket.enums.VehicleSeatTypeEnum;
import org.zys.railway_12306.service.ticket.enums.VehicleTypeEnum;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.PurchaseTicketPassengerDetailDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.TrainSeatBaseDTO;
import org.zys.railway_12306.service.ticket.service.SeatService;
import org.zys.railway_12306.service.ticket.service.handler.ticket.base.AbstractTrainPurchaseTicketTemplate;
import org.zys.railway_12306.service.ticket.service.handler.ticket.base.BitMapCheckSeat;
import org.zys.railway_12306.service.ticket.service.handler.ticket.base.BitMapCheckSeatStatusFactory;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.SelectSeatDTO;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import org.zys.railway_12306.service.ticket.service.handler.ticket.select.SeatSelection;
import org.zys.railway_12306.service.ticket.toolkit.CarriageVacantSeatCalculateUtil;
import org.zys.railway_12306.service.ticket.toolkit.SeatNumberUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.zys.railway_12306.service.ticket.service.handler.ticket.base.BitMapCheckSeatStatusFactory.TRAIN_BUSINESS;

/**
 *高铁商务座购票组件
 *
 * @author SUM
 * @date 2026/03/21
 */
@Component
@RequiredArgsConstructor
public class TrainBusinessClassPurchaseTicketHandler extends AbstractTrainPurchaseTicketTemplate {

    /**
     * 座位服务实例，用于获取座位相关信息
     */
    private final SeatService seatService;

    /**
     * 商务座座位映射，将座位字母转换为数组索引
     * A -> 0, C -> 1, F -> 2
     */
    private static final Map<Character, Integer> SEAT_Y_INT = Map.of('A', 0, 'C', 1, 'F', 2);

    /**
     * 获取策略标识
     * <p>
     * 组合列车类型和座位类型作为策略标识
     * </p>
     *
     * @return 策略标识
     */
    @Override
    public String mark() {
        return VehicleTypeEnum.HIGH_SPEED_RAIN.getName() + VehicleSeatTypeEnum.BUSINESS_CLASS.getName();
    }
    
    /**
     * 选择座位
     * <p>
     * 根据乘客数量和是否有选择座位，调用不同的座位选择方法
     * </p>
     *
     * @param requestParam 购票请求入参
     * @return 乘车人座位信息列表
     */
    @Override
    protected List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam) {
        // 获取列车ID、出发站、到达站
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        
        // 获取乘客座位详情
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        
        // 获取可用车厢列表
        List<String> trainCarriageList = seatService.listUsableCarriageNumber(trainId, requestParam.getSeatType(), departure, arrival);
        
        // 获取每个车厢的余票数量
        List<Integer> trainStationCarriageRemainingTicket = seatService.listSeatRemainingTicket(trainId, departure, arrival, trainCarriageList);
        
        // 计算总余票数量
        int remainingTicketSum = trainStationCarriageRemainingTicket.stream().mapToInt(Integer::intValue).sum();
        
        // 检查余票是否足够
        if (remainingTicketSum < passengerSeatDetails.size()) {
            throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
        }
        
        // 根据乘客数量和是否有选择座位，调用不同的座位选择方法
        if (passengerSeatDetails.size() < 3) {
            if (CollUtil.isNotEmpty(requestParam.getRequestParam().getChooseSeats())) {
                // 乘客数量小于3且有选择座位，调用 findMatchSeats 方法
                Pair<List<TrainPurchaseTicketRespDTO>, Boolean> actualSeatPair = findMatchSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
                return actualSeatPair.getKey();
            }
            // 乘客数量小于3且没有选择座位，调用 selectSeats 重载方法
            return selectSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
        } else {
            if (CollUtil.isNotEmpty(requestParam.getRequestParam().getChooseSeats())) {
                // 乘客数量大于等于3且有选择座位，调用 findMatchSeats 方法
                Pair<List<TrainPurchaseTicketRespDTO>, Boolean> actualSeatPair = findMatchSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
                return actualSeatPair.getKey();
            }
            // 乘客数量大于等于3且没有选择座位，调用 selectComplexSeats 方法
            return selectComplexSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
        }
    }

    /**
     * 寻找匹配的座位
     * <p>
     * 根据用户选择的座位，在可用车厢中寻找匹配的座位
     * </p>
     *
     * @param requestParam 购票请求入参
     * @param trainCarriageList 可用车厢列表
     * @param trainStationCarriageRemainingTicket 每个车厢的余票数量
     * @return 座位分配结果和是否成功
     */
    private Pair<List<TrainPurchaseTicketRespDTO>, Boolean> findMatchSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        // 构建 TrainSeatBaseDTO 对象
        TrainSeatBaseDTO trainSeatBaseDTO = buildTrainSeatBaseDTO(requestParam);
        
        // 获取选择座位的数量
        int chooseSeatSize = trainSeatBaseDTO.getChooseSeatList().size();
        
        // 初始化结果列表
        List<TrainPurchaseTicketRespDTO> actualResult = Lists.newArrayListWithCapacity(trainSeatBaseDTO.getPassengerSeatDetails().size());
        
        // 获取商务座位图检查实例
        BitMapCheckSeat instance = BitMapCheckSeatStatusFactory.getInstance(TRAIN_BUSINESS);
        
        // 存储车厢座位信息的映射
        HashMap<String, List<Pair<Integer, Integer>>> carriagesSeatMap = new HashMap<>(4);
        
        // 获取乘客数量
        int passengersNumber = trainSeatBaseDTO.getPassengerSeatDetails().size();
        
        // 遍历每个车厢
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            // 获取车厢号
            String carriagesNumber = trainCarriageList.get(i);
            
            // 获取车厢的可用座位
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainSeatBaseDTO.getTrainId(), carriagesNumber, requestParam.getSeatType(), trainSeatBaseDTO.getDeparture(), trainSeatBaseDTO.getArrival());
            
            // 初始化座位矩阵（商务座通常为2排3列）
            int[][] actualSeats = new int[2][3];
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 4; k++) {
                    // 标记座位是否可用（0表示可用，1表示已占用）
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            
            // 计算车厢的可用座位列表
            List<Pair<Integer, Integer>> vacantSeatList = CarriageVacantSeatCalculateUtil.buildCarriageVacantSeatList2(actualSeats, 2, 3);
            
            // 检查用户选择的座位是否存在
            boolean isExists = instance.checkChooseSeat(trainSeatBaseDTO.getChooseSeatList(), actualSeats, SEAT_Y_INT);
            
            // 获取可用座位数量
            long vacantSeatCount = vacantSeatList.size();
            
            // 初始化确定的座位列表
            List<Pair<Integer, Integer>> sureSeatList = new ArrayList<>();
            
            // 初始化选择的座位列表
            List<String> selectSeats = Lists.newArrayListWithCapacity(passengersNumber);
            
            // 标记是否需要继续查找
            boolean flag = false;
            
            // 如果用户选择的座位存在且可用座位数量足够
            if (isExists && vacantSeatCount >= passengersNumber) {
                // 获取可用座位的迭代器
                Iterator<Pair<Integer, Integer>> pairIterator = vacantSeatList.iterator();
                
                // 遍历用户选择的座位
                for (int i1 = 0; i1 < chooseSeatSize; i1++) {
                    if (chooseSeatSize == 1) {
                        // 如果只选择了一个座位
                        String chooseSeat = trainSeatBaseDTO.getChooseSeatList().get(i1);
                        int seatX = Integer.parseInt(chooseSeat.substring(1));  // 座位行号
                        int seatY = SEAT_Y_INT.get(chooseSeat.charAt(0));     // 座位列号
                        
                        if (actualSeats[seatX][seatY] == 0) {
                            // 座位可用，添加到确定的座位列表
                            sureSeatList.add(new Pair<>(seatX, seatY));
                            // 从可用座位列表中移除该座位
                            while (pairIterator.hasNext()) {
                                Pair<Integer, Integer> pair = pairIterator.next();
                                if (pair.getKey() == seatX && pair.getValue() == seatY) {
                                    pairIterator.remove();
                                    break;
                                }
                            }
                        } else {
                            // 座位不可用，尝试同一列的另一个座位
                            if (actualSeats[1][seatY] == 0) {
                                sureSeatList.add(new Pair<>(1, seatY));
                                // 从可用座位列表中移除该座位
                                while (pairIterator.hasNext()) {
                                    Pair<Integer, Integer> pair = pairIterator.next();
                                    if (pair.getKey() == 1 && pair.getValue() == seatY) {
                                        pairIterator.remove();
                                        break;
                                    }
                                }
                            } else {
                                // 同一列没有可用座位，标记需要继续查找
                                flag = true;
                            }
                        }
                    } else {
                        // 如果选择了多个座位
                        String chooseSeat = trainSeatBaseDTO.getChooseSeatList().get(i1);
                        int seatX = Integer.parseInt(chooseSeat.substring(1));  // 座位行号
                        int seatY = SEAT_Y_INT.get(chooseSeat.charAt(0));     // 座位列号
                        
                        if (actualSeats[seatX][seatY] == 0) {
                            // 座位可用，添加到确定的座位列表
                            sureSeatList.add(new Pair<>(seatX, seatY));
                            // 从可用座位列表中移除该座位
                            while (pairIterator.hasNext()) {
                                Pair<Integer, Integer> pair = pairIterator.next();
                                if (pair.getKey() == seatX && pair.getValue() == seatY) {
                                    pairIterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
                
                // 如果需要继续查找且不是最后一个车厢，跳过当前车厢
                if (flag && i < trainStationCarriageRemainingTicket.size() - 1) {
                    continue;
                }
                
                // 如果确定的座位数量不足，从可用座位中补充
                if (sureSeatList.size() != passengersNumber) {
                    int needSeatSize = passengersNumber - sureSeatList.size();
                    sureSeatList.addAll(vacantSeatList.subList(0, needSeatSize));
                }
                
                // 将确定的座位转换为座位号
                for (Pair<Integer, Integer> each : sureSeatList) {
                    selectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, (each.getValue() + 1)));
                }
                
                // 为每个乘客分配座位
                AtomicInteger countNum = new AtomicInteger(0);
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(carriagesNumber);
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
                
                // 返回座位分配结果
                return new Pair<>(actualResult, Boolean.TRUE);
            } else {
                // 如果用户选择的座位不存在或可用座位数量不足
                if (i < trainStationCarriageRemainingTicket.size()) {
                    // 如果当前车厢有可用座位，将其添加到车厢座位映射中
                    if (vacantSeatCount > 0) {
                        carriagesSeatMap.put(carriagesNumber, vacantSeatList);
                    }
                    
                    // 如果是最后一个车厢，尝试从所有车厢中分配座位
                    if (i == trainStationCarriageRemainingTicket.size() - 1) {
                        // 查找有足够座位的车厢
                        Pair<String, List<Pair<Integer, Integer>>> findSureCarriage = null;
                        for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : carriagesSeatMap.entrySet()) {
                            if (entry.getValue().size() >= passengersNumber) {
                                findSureCarriage = new Pair<>(entry.getKey(), entry.getValue().subList(0, passengersNumber));
                                break;
                            }
                        }
                        
                        if (null != findSureCarriage) {
                            // 找到有足够座位的车厢，分配座位
                            sureSeatList = findSureCarriage.getValue().subList(0, passengersNumber);
                            for (Pair<Integer, Integer> each : sureSeatList) {
                                selectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, each.getValue() + 1));
                            }
                            AtomicInteger countNum = new AtomicInteger(0);
                            for (String selectSeat : selectSeats) {
                                TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                                PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                                result.setSeatNumber(selectSeat);
                                result.setSeatType(currentTicketPassenger.getSeatType());
                                result.setCarriageNumber(findSureCarriage.getKey());
                                result.setPassengerId(currentTicketPassenger.getPassengerId());
                                actualResult.add(result);
                            }
                        } else {
                            // 没有找到有足够座位的车厢，尝试从多个车厢分配座位
                            int sureSeatListSize = 0;
                            AtomicInteger countNum = new AtomicInteger(0);
                            for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : carriagesSeatMap.entrySet()) {
                                if (sureSeatListSize < passengersNumber) {
                                    if (sureSeatListSize + entry.getValue().size() < passengersNumber) {
                                        // 当前车厢的座位全部使用
                                        sureSeatListSize = sureSeatListSize + entry.getValue().size();
                                        List<String> actualSelectSeats = new ArrayList<>();
                                        for (Pair<Integer, Integer> each : entry.getValue()) {
                                            actualSelectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, each.getValue() + 1));
                                        }
                                        for (String selectSeat : actualSelectSeats) {
                                            TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                                            PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                                            result.setSeatNumber(selectSeat);
                                            result.setSeatType(currentTicketPassenger.getSeatType());
                                            result.setCarriageNumber(entry.getKey());
                                            result.setPassengerId(currentTicketPassenger.getPassengerId());
                                            actualResult.add(result);
                                        }
                                    } else {
                                        // 当前车厢的座位部分使用
                                        int needSeatSize = entry.getValue().size() - (sureSeatListSize + entry.getValue().size() - passengersNumber);
                                        sureSeatListSize = sureSeatListSize + needSeatSize;
                                        if (sureSeatListSize >= passengersNumber) {
                                            List<String> actualSelectSeats = new ArrayList<>();
                                            for (Pair<Integer, Integer> each : entry.getValue().subList(0, needSeatSize)) {
                                                actualSelectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, each.getValue() + 1));
                                            }
                                            for (String selectSeat : actualSelectSeats) {
                                                TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                                                PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                                                result.setSeatNumber(selectSeat);
                                                result.setSeatType(currentTicketPassenger.getSeatType());
                                                result.setCarriageNumber(entry.getKey());
                                                result.setPassengerId(currentTicketPassenger.getPassengerId());
                                                actualResult.add(result);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        // 返回座位分配结果
                        return new Pair<>(actualResult, Boolean.TRUE);
                    }
                }
            }
        }
        // 没有找到可用座位
        return new Pair<>(null, Boolean.FALSE);
    }

    /**
     * 选择座位（重载方法）
     * <p>
     * 用于乘客数量小于3且没有选择座位的情况
     * </p>
     *
     * @param requestParam 购票请求入参
     * @param trainCarriageList 可用车厢列表
     * @param trainStationCarriageRemainingTicket 每个车厢的余票数量
     * @return 乘车人座位信息列表
     */
    private List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        // 获取列车ID、出发站、到达站
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        
        // 获取乘客座位详情
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        
        // 初始化结果列表
        List<TrainPurchaseTicketRespDTO> actualResult = new ArrayList<>();
        
        // 存储车厢余票数量的映射
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        
        // 存储车厢座位矩阵的映射
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
        
        // 存储车厢分配座位的映射
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
        
        String carriagesNumber;
        
        // 遍历每个车厢
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            // 获取车厢号
            carriagesNumber = trainCarriageList.get(i);
            
            // 获取车厢的可用座位
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainId, carriagesNumber, requestParam.getSeatType(), departure, arrival);
            
            // 初始化座位矩阵（商务座通常为2排3列）
            int[][] actualSeats = new int[2][3];
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 4; k++) {
                    // 当前默认按照复兴号商务座排序，后续这里需要按照简单工厂对车类型进行获取 y 轴
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            
            // 尝试分配邻座
            int[][] select = SeatSelection.adjacent(passengerSeatDetails.size(), actualSeats);
            if (select != null) {
                // 找到邻座，存储分配结果
                carriagesNumberSeatsMap.put(carriagesNumber, select);
                break;
            }
            
            // 计算车厢的可用座位数量
            int demotionStockNum = 0;
            for (int[] actualSeat : actualSeats) {
                for (int i1 : actualSeat) {
                    if (i1 == 0) {
                        demotionStockNum++;
                    }
                }
            }
            
            // 存储车厢余票数量和座位矩阵
            demotionStockNumMap.putIfAbsent(carriagesNumber, demotionStockNum);
            actualSeatsMap.putIfAbsent(carriagesNumber, actualSeats);
            
            // 如果不是最后一个车厢，继续下一个车厢
            if (i < trainStationCarriageRemainingTicket.size() - 1) {
                continue;
            }
            
            // 如果邻座算法无法匹配，尝试对用户进行降级分配：同车厢不邻座
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                if (demotionStockNumBack > passengerSeatDetails.size()) {
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(passengerSeatDetails.size(), seats);
                    if (Objects.equals(nonAdjacentSeats.length, passengerSeatDetails.size())) {
                        select = nonAdjacentSeats;
                        carriagesNumberSeatsMap.put(carriagesNumberBack, select);
                        break;
                    }
                }
            }
            
            // 如果同车厢也已无法匹配，则对用户座位再次降级：不同车厢不邻座
            if (Objects.isNull(select)) {
                for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                    String carriagesNumberBack = entry.getKey();
                    int demotionStockNumBack = entry.getValue();
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(demotionStockNumBack, seats);
                    carriagesNumberSeatsMap.put(entry.getKey(), nonAdjacentSeats);
                }
            }
        }
        
        // 乘车人员在单一车厢座位不满足，触发乘车人元分布在不同车厢
        int count = (int) carriagesNumberSeatsMap.values().stream()
                .flatMap(Arrays::stream)
                .count();
        
        if (CollUtil.isNotEmpty(carriagesNumberSeatsMap) && passengerSeatDetails.size() == count) {
            int countNum = 0;
            for (Map.Entry<String, int[][]> entry : carriagesNumberSeatsMap.entrySet()) {
                List<String> selectSeats = new ArrayList<>();
                for (int[] ints : entry.getValue()) {
                    selectSeats.add("0" + ints[0] + SeatNumberUtil.convert(0, ints[1]));
                }
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = passengerSeatDetails.get(countNum++);
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(entry.getKey());
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
            }
        }
        return actualResult;
    }

    /**
     * 选择复杂座位
     * <p>
     * 用于乘客数量大于等于3且没有选择座位的情况
     * </p>
     *
     * @param requestParam 购票请求入参
     * @param trainCarriageList 可用车厢列表
     * @param trainStationCarriageRemainingTicket 每个车厢的余票数量
     * @return 乘车人座位信息列表
     */
    private List<TrainPurchaseTicketRespDTO> selectComplexSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        // 获取列车ID、出发站、到达站
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        
        // 获取乘客座位详情
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        
        // 初始化结果列表
        List<TrainPurchaseTicketRespDTO> actualResult = new ArrayList<>();
        
        // 存储车厢余票数量的映射
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        
        // 存储车厢座位矩阵的映射
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
        
        // 存储车厢分配座位的映射
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
        
        String carriagesNumber;
        
        // 多人分配同一车厢邻座
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            // 获取车厢号
            carriagesNumber = trainCarriageList.get(i);
            
            // 获取车厢的可用座位
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainId, carriagesNumber, requestParam.getSeatType(), departure, arrival);
            
            // 初始化座位矩阵（商务座通常为2排3列）
            int[][] actualSeats = new int[2][3];
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 4; k++) {
                    // 当前默认按照复兴号商务座排序，后续这里需要按照简单工厂对车类型进行获取 y 轴
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            
            // 深拷贝座位矩阵
            int[][] actualSeatsTranscript = deepCopy(actualSeats);
            
            // 存储实际选择的座位
            List<int[][]> actualSelects = new ArrayList<>();
            
            // 将乘客分组，每组2人
            List<List<PurchaseTicketPassengerDetailDTO>> splitPassengerSeatDetails = ListUtil.split(passengerSeatDetails, 2);
            
            // 为每组乘客分配邻座
            for (List<PurchaseTicketPassengerDetailDTO> each : splitPassengerSeatDetails) {
                int[][] select = SeatSelection.adjacent(each.size(), actualSeatsTranscript);
                if (select != null) {
                    // 标记已分配的座位
                    for (int[] ints : select) {
                        actualSeatsTranscript[ints[0] - 1][ints[1] - 1] = 1;
                    }
                    actualSelects.add(select);
                }
            }
            
            // 如果所有组都分配到了邻座
            if (actualSelects.size() == splitPassengerSeatDetails.size()) {
                int[][] actualSelect = null;
                // 合并所有分配的座位
                for (int j = 0; j < actualSelects.size(); j++) {
                    if (j == 0) {
                        actualSelect = mergeArrays(actualSelects.get(j), actualSelects.get(j + 1));
                    }
                    if (j != 0 && actualSelects.size() > 2) {
                        actualSelect = mergeArrays(actualSelect, actualSelects.get(j + 1));
                    }
                }
                // 存储分配结果
                carriagesNumberSeatsMap.put(carriagesNumber, actualSelect);
                break;
            }
            
            // 计算车厢的可用座位数量
            int demotionStockNum = 0;
            for (int[] actualSeat : actualSeats) {
                for (int i1 : actualSeat) {
                    if (i1 == 0) {
                        demotionStockNum++;
                    }
                }
            }
            
            // 存储车厢余票数量和座位矩阵
            demotionStockNumMap.putIfAbsent(carriagesNumber, demotionStockNum);
            actualSeatsMap.putIfAbsent(carriagesNumber, actualSeats);
        }
        
        // 如果邻座算法无法匹配，尝试对用户进行降级分配：同车厢不邻座
        if (CollUtil.isEmpty(carriagesNumberSeatsMap)) {
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                if (demotionStockNumBack > passengerSeatDetails.size()) {
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(passengerSeatDetails.size(), seats);
                    if (Objects.equals(nonAdjacentSeats.length, passengerSeatDetails.size())) {
                        carriagesNumberSeatsMap.put(carriagesNumberBack, nonAdjacentSeats);
                        break;
                    }
                }
            }
        }
        
        // 如果同车厢也已无法匹配，则对用户座位再次降级：不同车厢不邻座
        if (CollUtil.isEmpty(carriagesNumberSeatsMap)) {
            int undistributedPassengerSize = passengerSeatDetails.size();
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(Math.min(undistributedPassengerSize, demotionStockNumBack), seats);
                undistributedPassengerSize = undistributedPassengerSize - demotionStockNumBack;
                carriagesNumberSeatsMap.put(entry.getKey(), nonAdjacentSeats);
            }
        }
        
        // 乘车人员在单一车厢座位不满足，触发乘车人元分布在不同车厢
        int count = (int) carriagesNumberSeatsMap.values().stream()
                .flatMap(Arrays::stream)
                .count();
        
        if (CollUtil.isNotEmpty(carriagesNumberSeatsMap) && passengerSeatDetails.size() == count) {
            int countNum = 0;
            for (Map.Entry<String, int[][]> entry : carriagesNumberSeatsMap.entrySet()) {
                List<String> selectSeats = new ArrayList<>();
                for (int[] ints : entry.getValue()) {
                    selectSeats.add("0" + ints[0] + SeatNumberUtil.convert(0, ints[1]));
                }
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = passengerSeatDetails.get(countNum++);
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(entry.getKey());
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
            }
        }
        return actualResult;
    }

    /**
     * 合并两个二维数组
     *
     * @param array1 第一个二维数组
     * @param array2 第二个二维数组
     * @return 合并后的二维数组
     */
    public static int[][] mergeArrays(int[][] array1, int[][] array2) {
        List<int[]> list = new ArrayList<>(Arrays.asList(array1));
        list.addAll(Arrays.asList(array2));
        return list.toArray(new int[0][]);
    }

    /**
     * 深拷贝二维数组
     *
     * @param originalArray 原始二维数组
     * @return 拷贝后的二维数组
     */
    public static int[][] deepCopy(int[][] originalArray) {
        int[][] copy = new int[originalArray.length][originalArray[0].length];
        for (int i = 0; i < originalArray.length; i++) {
            System.arraycopy(originalArray[i], 0, copy[i], 0, originalArray[i].length);
        }
        return copy;
    }

}
