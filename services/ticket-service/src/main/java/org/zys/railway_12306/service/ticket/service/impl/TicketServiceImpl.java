package org.zys.railway_12306.service.ticket.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zys.rail_12306.framework.starter.bases.ApplicationContextHolder;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.rail_12306.framework.starter.cache.toolkit.CacheUtil;
import org.zys.rail_12306.framework.starter.designpattern.chain.AbstractChainContext;
import org.zys.rail_12306.framework.starter.idempotent.annotation.Idempotent;
import org.zys.rail_12306.framework.starter.idempotent.enums.IdempotentSceneEnum;
import org.zys.rail_12306.framework.starter.idempotent.enums.IdempotentTypeEnum;
import org.zys.rail_12306.framework.starter.log.annotation.ILog;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.user.core.UserContext;
import org.zys.railway_12306.service.ticket.enums.SourceEnum;
import org.zys.railway_12306.service.ticket.enums.TicketChainEnum;
import org.zys.railway_12306.service.ticket.enums.TicketStatusEnum;
import org.zys.railway_12306.service.ticket.mapper.StationMapper;
import org.zys.railway_12306.service.ticket.mapper.TicketMapper;
import org.zys.railway_12306.service.ticket.mapper.TrainMapper;
import org.zys.railway_12306.service.ticket.mapper.TrainStationPriceMapper;
import org.zys.railway_12306.service.ticket.mapper.TrainStationRelationMapper;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.RouteDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.SeatClassDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.SeatTypeCountDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.TicketListDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.CancelTicketOrderReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.PurchaseTicketReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.RefundTicketReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.TicketPageQueryReqDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.RefundTicketRespDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.TicketOrderDetailRespDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.TicketPageQueryRespDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.resp.TicketPurchaseRespDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.Station;
import org.zys.railway_12306.service.ticket.pojo.entity.Ticket;
import org.zys.railway_12306.service.ticket.pojo.entity.Train;
import org.zys.railway_12306.service.ticket.pojo.entity.TrainStationPrice;
import org.zys.railway_12306.service.ticket.pojo.entity.TrainStationRelation;
import org.zys.railway_12306.service.ticket.remote.TicketOrderRemoteService;
import org.zys.railway_12306.service.ticket.remote.dto.PayInfoRespDTO;
import org.zys.railway_12306.service.ticket.remote.dto.TicketOrderCreateRemoteReqDTO;
import org.zys.railway_12306.service.ticket.remote.dto.TicketOrderItemCreateRemoteReqDTO;
import org.zys.railway_12306.service.ticket.remote.dto.TicketOrderPassengerDetailRespDTO;
import org.zys.railway_12306.service.ticket.service.SeatService;
import org.zys.railway_12306.service.ticket.service.TicketService;
import org.zys.railway_12306.service.ticket.service.TrainStationService;
import org.zys.railway_12306.service.ticket.service.cache.SeatMarginCacheLoader;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.TokenResultDTO;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import org.zys.railway_12306.service.ticket.service.handler.ticket.select.TrainSeatTypeSelector;
import org.zys.railway_12306.service.ticket.service.handler.ticket.tokenbucket.TicketAvailabilityTokenBucket;
import org.zys.railway_12306.service.ticket.toolkit.DateUtil;
import org.zys.railway_12306.service.ticket.toolkit.TimeStringComparator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.zys.railway_12306.service.ticket.constant.Railway12306Constant.ADVANCE_TICKET_DAY;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.LOCK_PURCHASE_TICKETS;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.LOCK_REGION_TRAIN_STATION;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.LOCK_REGION_TRAIN_STATION_MAPPING;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.LOCK_TOKEN_BUCKET_ISNULL;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.REGION_TRAIN_STATION;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.REGION_TRAIN_STATION_MAPPING;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.TRAIN_INFO;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.TRAIN_STATION_PRICE;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.TRAIN_STATION_REMAINING_TICKET;
import static org.zys.railway_12306.service.ticket.toolkit.DateUtil.convertDateToLocalTime;


/**
 *车票接口实现
 *
 * @author SUM
 * @date 2026/03/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService, CommandLineRunner {

    private TicketService ticketService;
    private final AbstractChainContext<TicketPageQueryReqDTO> ticketPageQueryAbstractChainContext;
    private final DistributedCache distributedCache;
    private final RedissonClient redissonClient;
    private final StationMapper stationMapper;
    private final TrainStationRelationMapper trainStationRelationMapper;
    private final TrainMapper trainMapper;
    private final TrainStationPriceMapper trainStationPriceMapper;
    private final SeatMarginCacheLoader seatMarginCacheLoader;
    private final AbstractChainContext<PurchaseTicketReqDTO> purchaseTicketAbstractChainContext;
    private final ConfigurableEnvironment environment;
    private final TicketAvailabilityTokenBucket ticketAvailabilityTokenBucket;
    private final SeatService seatService;
    private final TrainSeatTypeSelector trainSeatTypeSelector;
    private final TicketOrderRemoteService ticketOrderRemoteService;
    private final TrainStationService trainStationService;


    @Value("${ticket.availability.cache-update.type:}")
    private String ticketAvailabilityCacheUpdateType;



    /**
     * 火车票查询方法 V1 版本
     * @param requestParam 查询请求参数，包含出发站、到达站、出发日期等信息
     * @return 火车票查询响应，包含列车列表、出发站列表、到达站列表等信息
     */
    @Override
    public TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam) {
        // 责任链模式 验证城市名称是否存在、不存在加载缓存以及出发日期不能小于当前日期等等
        // 1. 调用链式处理上下文，执行火车查询过滤逻辑
        ticketPageQueryAbstractChainContext.handler(TicketChainEnum.TRAIN_QUERY_FILTER.name(), requestParam);

        // 2. 获取 StringRedisTemplate 实例，用于操作 Redis
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();

        // 3. 从 Redis 哈希表中获取出发站和到达站的区域信息
        List<Object> stationDetails = stringRedisTemplate.opsForHash()
                .multiGet(REGION_TRAIN_STATION_MAPPING, Lists.newArrayList(requestParam.getFromStation(), requestParam.getToStation()));

        // 4. 检查获取的车站信息是否有缺失，统计列表中为 null 的元素数量
        long count = stationDetails.stream().filter(Objects::isNull).count();
        if (count > 0) {
            // 缓存里没有，需要去数据库加载
            // 5. 获取分布式锁，防止并发加载车站信息
            RLock lock = redissonClient.getLock(LOCK_REGION_TRAIN_STATION_MAPPING);
            lock.lock();
            try {
                // 6. 再次检查缓存，防止缓存穿透（防止在获取锁的瞬间，缓存已经被其他线程加载了）
                stationDetails = stringRedisTemplate.opsForHash()
                        .multiGet(REGION_TRAIN_STATION_MAPPING, Lists.newArrayList(requestParam.getFromStation(), requestParam.getToStation()));
                count = stationDetails.stream().filter(Objects::isNull).count();
                if (count > 0) {
                    // 7. 从数据库查询所有车站信息
                    List<Station> stationList = stationMapper.selectList(Wrappers.emptyWrapper());
                    Map<String, String> regionTrainStationMap = new HashMap<>();
                    // 8. 构建车站代码到区域名称的映射
                    stationList.forEach(each -> regionTrainStationMap.put(each.getCode(), each.getRegionName()));
                    // 9. 将映射信息存入 Redis 哈希表
                    stringRedisTemplate.opsForHash().putAll(REGION_TRAIN_STATION_MAPPING, regionTrainStationMap);
                    // 10. 更新车站区域信息列表
                    stationDetails = new ArrayList<>();
                    stationDetails.add(regionTrainStationMap.get(requestParam.getFromStation()));
                    stationDetails.add(regionTrainStationMap.get(requestParam.getToStation()));
                }
            } finally {
                // 11. 释放分布式锁
                lock.unlock();
            }
        }

        // 12. 初始化列车列表结果
        List<TicketListDTO> seatResults = new ArrayList<>();

        // 13. 构建区域间火车站的哈希键，格式为：region:train:station:{出发区域}:{到达区域}
        String buildRegionTrainStationHashKey = String.format(REGION_TRAIN_STATION, stationDetails.get(0), stationDetails.get(1));

        // 14. 从 Redis 哈希表中获取区域间的所有列车信息
        Map<Object, Object> regionTrainStationAllMap = stringRedisTemplate.opsForHash().entries(buildRegionTrainStationHashKey);

        // 15. 如果缓存中没有列车信息，则从数据库查询
        if (MapUtil.isEmpty(regionTrainStationAllMap)) {
            // 16. 获取分布式锁，防止并发加载列车信息
            RLock lock = redissonClient.getLock(LOCK_REGION_TRAIN_STATION);
            lock.lock();
            try {
                // 17. 再次检查缓存，防止缓存穿透
                regionTrainStationAllMap = stringRedisTemplate.opsForHash().entries(buildRegionTrainStationHashKey);
                if (MapUtil.isEmpty(regionTrainStationAllMap)) {
                    // 18. 构建查询条件，查询指定区域间的列车站点关系
                    LambdaQueryWrapper<TrainStationRelation> queryWrapper = Wrappers.lambdaQuery(TrainStationRelation.class)
                            .eq(TrainStationRelation::getStartRegion, stationDetails.get(0))
                            .eq(TrainStationRelation::getEndRegion, stationDetails.get(1));
                    List<TrainStationRelation> trainStationRelationList = trainStationRelationMapper.selectList(queryWrapper);

                    // 19. 遍历列车站点关系，构建列车信息
                    for (TrainStationRelation each : trainStationRelationList) {
                        // 20. 从缓存或数据库获取列车信息
                        Train train = distributedCache.safeGet(
                                TRAIN_INFO + each.getTrainId(),  // 缓存键：train:info:{trainId}
                                Train.class,
                                () -> trainMapper.selectById(each.getTrainId()),  // 缓存未命中时从数据库查询
                                ADVANCE_TICKET_DAY,  // 缓存过期时间：预售天数
                                TimeUnit.DAYS);

                        // 21. 构建列车列表DTO
                        TicketListDTO result = new TicketListDTO();
                        result.setTrainId(String.valueOf(train.getId()));
                        result.setTrainNumber(train.getTrainNumber());
                        result.setDepartureTime(convertDateToLocalTime(each.getDepartureTime(), "HH:mm"));
                        result.setArrivalTime(convertDateToLocalTime(each.getArrivalTime(), "HH:mm"));
                        result.setDuration(DateUtil.calculateHourDifference(each.getDepartureTime(), each.getArrivalTime()));
                        result.setDeparture(each.getDeparture());
                        result.setArrival(each.getArrival());
                        result.setDepartureFlag(each.getDepartureFlag());
                        result.setArrivalFlag(each.getArrivalFlag());
                        result.setTrainType(train.getTrainType());
                        result.setTrainBrand(train.getTrainBrand());
                        if (StrUtil.isNotBlank(train.getTrainTag())) {
                            result.setTrainTags(StrUtil.split(train.getTrainTag(), ","));
                        }
                        // 22. 计算行程天数
                        long betweenDay = cn.hutool.core.date.DateUtil.betweenDay(each.getDepartureTime(), each.getArrivalTime(), false);
                        result.setDaysArrived((int) betweenDay);
                        // 23. 设置售票状态：0-可售，1-不可售
                        result.setSaleStatus(new Date().after(train.getSaleTime()) ? 0 : 1);
                        result.setSaleTime(convertDateToLocalTime(train.getSaleTime(), "MM-dd HH:mm"));

                        // 24. 添加到结果列表
                        seatResults.add(result);
                        // 25. 将列车信息存入缓存，键为：{trainId}_{departure}_{arrival}
                        regionTrainStationAllMap.put(CacheUtil.buildKey(String.valueOf(each.getTrainId()), each.getDeparture(), each.getArrival()), JSON.toJSONString(result));
                    }
                    // 26. 将所有列车信息批量存入 Redis 哈希表
                    stringRedisTemplate.opsForHash().putAll(buildRegionTrainStationHashKey, regionTrainStationAllMap);
                }
            } finally {
                // 27. 释放分布式锁
                lock.unlock();
            }
        }

        // 28. 如果 seatResults 为空，则从缓存中解析列车信息
        seatResults = CollUtil.isEmpty(seatResults)
                ? regionTrainStationAllMap.values().stream().map(each -> JSON.parseObject(each.toString(), TicketListDTO.class)).toList()
                : seatResults;

        // 29. 按出发时间对列车列表进行排序
        seatResults = seatResults.stream().sorted(new TimeStringComparator()).toList();

        // 30. 为每个列车添加座位信息
        for (TicketListDTO each : seatResults) {
            // 31. 从缓存或数据库获取票价信息
            String trainStationPriceStr = distributedCache.safeGet(
                    // 缓存键：train:station:price:{trainId}:{departure}:{arrival}
                    String.format(TRAIN_STATION_PRICE, each.getTrainId(), each.getDeparture(), each.getArrival()),
                    String.class,
                    () -> {
                        // 缓存未命中时从数据库查询
                        LambdaQueryWrapper<TrainStationPrice> trainStationPriceQueryWrapper = Wrappers.lambdaQuery(TrainStationPrice.class)
                                .eq(TrainStationPrice::getDeparture, each.getDeparture())
                                .eq(TrainStationPrice::getArrival, each.getArrival())
                                .eq(TrainStationPrice::getTrainId, each.getTrainId());
                        return JSON.toJSONString(trainStationPriceMapper.selectList(trainStationPriceQueryWrapper));
                    },
                    // 缓存过期时间：预售天数
                    ADVANCE_TICKET_DAY,
                    TimeUnit.DAYS
            );

            // 32. 解析票价信息
            List<TrainStationPrice> trainStationPriceList = JSON.parseArray(trainStationPriceStr, TrainStationPrice.class);
            List<SeatClassDTO> seatClassList = new ArrayList<>();

            // 33. 遍历票价信息，构建座位类型列表
            trainStationPriceList.forEach(item -> {
                String seatType = String.valueOf(item.getSeatType());
                String keySuffix = StrUtil.join("_", each.getTrainId(), item.getDeparture(), item.getArrival());

                // 34. 从 Redis 哈希表中获取剩余票数，键为：train:station:remaining:ticket:{keySuffix}
                Object quantityObj = stringRedisTemplate.opsForHash().get(TRAIN_STATION_REMAINING_TICKET + keySuffix, seatType);
                int quantity = Optional.ofNullable(quantityObj)
                        .map(Object::toString)
                        .map(Integer::parseInt)
                        .orElseGet(() -> {
                            // 35. 如果缓存中没有剩余票数，则从缓存加载器获取
                            Map<String, String> seatMarginMap = seatMarginCacheLoader.load(String.valueOf(each.getTrainId()), seatType, item.getDeparture(), item.getArrival());
                            return Optional.ofNullable(seatMarginMap.get(String.valueOf(item.getSeatType()))).map(Integer::parseInt).orElse(0);
                        });

                // 36. 构建座位类型DTO，价格单位转换为元（数据库中存储的是分）
                seatClassList.add(new SeatClassDTO(item.getSeatType(), quantity, new BigDecimal(item.getPrice()).divide(new BigDecimal("100"), 1, RoundingMode.HALF_UP), false));
            });

            // 37. 设置列车的座位类型列表
            each.setSeatClassList(seatClassList);
        }

        // 38. 构建并返回响应DTO
        return TicketPageQueryRespDTO.builder()
                .trainList(seatResults)  // 列车列表
                .departureStationList(buildDepartureStationList(seatResults))  // 出发站列表
                .arrivalStationList(buildArrivalStationList(seatResults))  // 到达站列表
                .trainBrandList(buildTrainBrandList(seatResults))  // 列车品牌列表
                .seatClassTypeList(buildSeatClassList(seatResults))  // 座位类型列表
                .build();
    }



    /**
     * 购票方法 V1 版本
     * @param requestParam 购票请求参数
     * @return 购票响应
     */
    @ILog  // 日志注解，记录方法调用
    @Idempotent(  // 幂等性注解，防止重复提交
            uniqueKeyPrefix = "railway_12306-ticket:lock_purchase-tickets:",
            key = "T(org.zys.rail_12306.framework.starter.bases.ApplicationContextHolder).getBean('environment').getProperty('unique-name', '')"
                    + "+'_'+"
                    + "T(org.zys.rail_12306.framework.starter.user.core.UserContext).getUsername()",
            message = "正在执行下单流程，请稍后...",
            scene = IdempotentSceneEnum.RESTAPI,
            type = IdempotentTypeEnum.SPEL
    )
    @Override
    public TicketPurchaseRespDTO purchaseTicketsV1(PurchaseTicketReqDTO requestParam) {
        // 责任链模式，验证 1：参数必填 2：参数正确性 3：乘客是否已买当前车次等...
        purchaseTicketAbstractChainContext.handler(TicketChainEnum.TRAIN_PURCHASE_TICKET_FILTER.name(), requestParam);
        // v1 版本购票存在 4 个较为严重的问题，v2 版本相比较 v1 版本更具有业务特点以及性能，整体提升较大
        // 构建分布式锁键：lock:purchase:tickets:{trainId}
        String lockKey = environment.resolvePlaceholders(String.format(LOCK_PURCHASE_TICKETS, requestParam.getTrainId()));
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            // 调用执行购票方法
            return ticketService.executePurchaseTickets(requestParam);
        } finally {
            // 释放分布式锁
            lock.unlock();
        }
    }


    /**
     * 执行购票操作
     * <p>
     * 该方法是购票流程的核心实现，主要完成以下步骤：
     * 1. 获取列车信息（优先从缓存获取）
     * 2. 根据列车类型选择合适的座位
     * 3. 创建车票记录
     * 4. 构建订单信息并调用远程订单服务
     * 5. 返回购票结果
     * </p>
     * @param requestParam 购票请求参数，包含车次、出发站、到达站、乘客信息等
     * @return 购票响应，包含订单号和订单详情
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)  // 事务注解，发生任何异常都回滚
    public TicketPurchaseRespDTO executePurchaseTickets(PurchaseTicketReqDTO requestParam) {
        // 1. 初始化订单详情结果列表，用于存储购票成功后的订单详情
        List<TicketOrderDetailRespDTO> ticketOrderDetailResults = new ArrayList<>();

        // 2. 获取列车ID，从请求参数中提取
        String trainId = requestParam.getTrainId();

        // 3. 从缓存或数据库获取列车信息
        // 使用safeGet方法，实现缓存穿透防护和自动缓存更新
        Train train = distributedCache.safeGet(
                TRAIN_INFO + trainId,  // 缓存键：train:info:{trainId}
                Train.class,           // 缓存值类型
                () -> trainMapper.selectById(trainId),  // 缓存未命中时从数据库查询
                ADVANCE_TICKET_DAY,    // 缓存过期时间：预售天数
                TimeUnit.DAYS);        // 时间单位：天

        // 4. 根据列车类型和请求参数选择座位
        // 使用策略模式，根据列车类型选择不同的座位分配策略
        List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults = trainSeatTypeSelector.select(train.getTrainType(), requestParam);

        // 5. 构建车票列表
        // 将座位选择结果转换为Ticket对象列表
        List<Ticket> ticketList  = trainPurchaseTicketResults.stream()
                .map(each -> Ticket.builder()
                        .username(UserContext.getUsername())  // 用户名，从上下文获取
                        .trainId(Long.parseLong(requestParam.getTrainId()))  // 列车ID
                        .carriageNumber(each.getCarriageNumber())  // 车厢号
                        .seatNumber(each.getSeatNumber())  // 座位号
                        .passengerId(each.getPassengerId())  // 乘客ID
                        .ticketStatus(TicketStatusEnum.UNPAID.getCode())  // 车票状态：未支付
                        .build())
                .toList();

        // 6. 批量保存车票信息到数据库
        saveBatch(ticketList);

        // 7. 初始化订单结果变量，用于接收远程订单服务的返回值
        Result<String> ticketOrderResult;

        try {
            // 8. 初始化订单项列表，用于构建远程订单请求
            List<TicketOrderItemCreateRemoteReqDTO> orderItemCreateRemoteReqDTOList = new ArrayList<>();

            // 9. 遍历购票结果，构建订单项和订单详情
            trainPurchaseTicketResults.forEach(each -> {
                // 9.1 构建远程订单项请求DTO，用于调用订单服务
                TicketOrderItemCreateRemoteReqDTO orderItemCreateRemoteReqDTO = TicketOrderItemCreateRemoteReqDTO.builder()
                        .amount(each.getAmount())  // 金额
                        .carriageNumber(each.getCarriageNumber())  // 车厢号
                        .seatNumber(each.getSeatNumber())  // 座位号
                        .idCard(each.getIdCard())  // 身份证号
                        .idType(each.getIdType())  // 证件类型
                        .phone(each.getPhone())  // 手机号
                        .seatType(each.getSeatType())  // 座位类型
                        .ticketType(each.getUserType())  // 车票类型
                        .realName(each.getRealName())  // 真实姓名
                        .build();

                // 9.2 构建订单详情DTO，用于返回给前端
                TicketOrderDetailRespDTO ticketOrderDetailRespDTO = TicketOrderDetailRespDTO.builder()
                        .amount(each.getAmount())  // 金额
                        .carriageNumber(each.getCarriageNumber())  // 车厢号
                        .seatNumber(each.getSeatNumber())  // 座位号
                        .idCard(each.getIdCard())  // 身份证号
                        .idType(each.getIdType())  // 证件类型
                        .seatType(each.getSeatType())  // 座位类型
                        .ticketType(each.getUserType())  // 车票类型
                        .realName(each.getRealName())  // 真实姓名
                        .build();

                // 9.3 添加到对应的列表中
                orderItemCreateRemoteReqDTOList.add(orderItemCreateRemoteReqDTO);
                ticketOrderDetailResults.add(ticketOrderDetailRespDTO);
            });

            // 10. 构建查询条件，查询列车站点关系
            // 根据列车ID、出发站和到达站查询列车站点关系信息
            LambdaQueryWrapper<TrainStationRelation> queryWrapper = Wrappers.lambdaQuery(TrainStationRelation.class)
                    .eq(TrainStationRelation::getTrainId, trainId)
                    .eq(TrainStationRelation::getDeparture, requestParam.getDeparture())
                    .eq(TrainStationRelation::getArrival, requestParam.getArrival());
            TrainStationRelation trainStationRelation = trainStationRelationMapper.selectOne(queryWrapper);

            // 11. 构建远程订单创建请求DTO
            // 组装订单创建所需的所有信息
            TicketOrderCreateRemoteReqDTO orderCreateRemoteReqDTO = TicketOrderCreateRemoteReqDTO.builder()
                    .departure(requestParam.getDeparture())  // 出发站
                    .arrival(requestParam.getArrival())  // 到达站
                    .orderTime(new Date())  // 订单时间
                    .source(SourceEnum.INTERNET.getCode())  // 订单来源：互联网
                    .trainNumber(train.getTrainNumber())  // 车次
                    .departureTime(trainStationRelation.getDepartureTime())  // 出发时间
                    .arrivalTime(trainStationRelation.getArrivalTime())  // 到达时间
                    .ridingDate(trainStationRelation.getDepartureTime())  // 乘车日期
                    .userId(UserContext.getUserId())  // 用户ID，从上下文获取
                    .username(UserContext.getUsername())  // 用户名，从上下文获取
                    .trainId(Long.parseLong(requestParam.getTrainId()))  // 列车ID
                    .ticketOrderItems(orderItemCreateRemoteReqDTOList)  // 订单项列表
                    .build();

            // 12. 调用远程订单服务创建订单
            // 通过Feign客户端调用订单服务的创建订单接口
            ticketOrderResult = ticketOrderRemoteService.createTicketOrder(orderCreateRemoteReqDTO);

            // 13. 检查订单创建结果
            // 验证订单创建是否成功，若失败则抛出异常
            if (!ticketOrderResult.isSuccess() || StrUtil.isBlank(ticketOrderResult.getData())) {
                log.error("订单服务调用失败，返回结果：{}", ticketOrderResult.getMessage());
                throw new ServiceException("订单服务调用失败");
            }
        } catch (Throwable ex) {
            // 14. 记录异常日志
            // 捕获并记录所有异常，然后重新抛出
            log.error("远程调用订单服务创建错误，请求参数：{}", JSON.toJSONString(requestParam), ex);
            throw ex;
        }

        // 15. 构建并返回购票响应
        // 将订单号和订单详情组装成响应对象返回
        return new TicketPurchaseRespDTO(ticketOrderResult.getData(), ticketOrderDetailResults);
    }

    /**
     * 取消车票订单
     * @param requestParam 取消订单请求参数
     */
    @ILog  // 日志注解，记录方法调用
    @Override
    public void cancelTicketOrder(CancelTicketOrderReqDTO requestParam) {
        //通过远程调用取消车票订单接口
        Result<Void> cancelOrderResult = ticketOrderRemoteService.cancelTicketOrder(requestParam);
        if (!cancelOrderResult.isSuccess() && StrUtil.equals(ticketAvailabilityCacheUpdateType,"binlog")) {
            //远程调用失败，并且缓存更新类型为binlog。执行以下逻辑
            //远程调用跟据订单号查询车票订单
            Result<org.zys.railway_12306.service.ticket.remote.dto.TicketOrderDetailRespDTO> ticketOrderDetailResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(requestParam.getOrderSn());
            org.zys.railway_12306.service.ticket.remote.dto.TicketOrderDetailRespDTO ticketOrderDetail = ticketOrderDetailResult.getData();
            String trainId = String.valueOf(ticketOrderDetail.getTrainId());
            String departure = ticketOrderDetail.getDeparture();
            String arrival = ticketOrderDetail.getArrival();
            List<TicketOrderPassengerDetailRespDTO> trainPurchaseTicketResults = ticketOrderDetail.getPassengerDetails();
            try {
                // 解锁座位
                seatService.unlock(trainId, departure, arrival, BeanUtil.convert(trainPurchaseTicketResults, TrainPurchaseTicketRespDTO.class));
            } catch (Throwable ex) {
                // 锁定座位失败，执行回滚逻辑
                log.error("[取消订单] 订单号：{} 回滚列车DB座位状态失败", requestParam.getOrderSn(), ex);
                throw ex;
            }

            try {
                //获取缓存组件实例
                StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
                //按席别类型分组
                Map<Integer, List<TicketOrderPassengerDetailRespDTO>> seatTypeMap = trainPurchaseTicketResults.stream()
                        .collect(Collectors.groupingBy(TicketOrderPassengerDetailRespDTO::getSeatType));
                List<RouteDTO> routeDTOList = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
                //循环每条路线
                routeDTOList.forEach(each -> {
                    String keySuffix = StrUtil.join("_", trainId, each.getStartStation(), each.getEndStation());
                    // 对每个席别类型，恢复相应数量的票数到 Redis 缓存
                    seatTypeMap.forEach((seatType, ticketOrderPassengerDetailRespDTOList) -> stringRedisTemplate.opsForHash()
                            .increment(TRAIN_STATION_REMAINING_TICKET + keySuffix, String.valueOf(seatType), ticketOrderPassengerDetailRespDTOList.size()));
                });
            } catch (Throwable ex) {
                log.error("[取消关闭订单] 订单号：{} 回滚列车Cache余票失败", requestParam.getOrderSn(), ex);
                throw ex;
            }
        }
    }

    /**
     * 获取支付信息
     * @param orderSn 订单编号
     * @return 支付信息响应
     */
    @Override
    public PayInfoRespDTO getPayInfo(String orderSn) {
        return null;  // 未实现
    }

    /**
     * 通用退票方法
     * @param requestParam 退票请求参数
     * @return 退票响应
     */
    @Override
    public RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam) {
        return null;  // 未实现
    }

    /**
     * 构建出发站点列表
     * @param seatResults 列车列表
     * @return 出发站点列表
     */
    private List<String> buildDepartureStationList(List<TicketListDTO> seatResults) {
        // 从列车列表中提取出发站，并去重
        return seatResults.stream().map(TicketListDTO::getDeparture).distinct().collect(Collectors.toList());
    }

    /**
     * 构建目的地站点列表
     * @param seatResults 列车列表
     * @return 目的地站点列表
     */
    private List<String> buildArrivalStationList(List<TicketListDTO> seatResults) {
        // 从列车列表中提取到达站，并去重
        return seatResults.stream().map(TicketListDTO::getArrival).distinct().collect(Collectors.toList());
    }

    /**
     * 构建座位类型列表
     * @param seatResults 列车列表
     * @return 座位类型列表
     */
    private List<Integer> buildSeatClassList(List<TicketListDTO> seatResults) {
        Set<Integer> resultSeatClassList = new HashSet<>();
        // 遍历所有列车的座位类型，添加到集合中去重
        for (TicketListDTO each : seatResults) {
            for (SeatClassDTO item : each.getSeatClassList()) {
                resultSeatClassList.add(item.getType());
            }
        }
        // 转换为列表并返回
        return resultSeatClassList.stream().toList();
    }

    /**
     * 构建列车品牌列表
     * @param seatResults 列车列表
     * @return 列车品牌列表
     */
    private List<Integer> buildTrainBrandList(List<TicketListDTO> seatResults) {
        Set<Integer> trainBrandSet = new HashSet<>();
        // 遍历所有列车的品牌信息，添加到集合中去重
        for (TicketListDTO each : seatResults) {
            if (StrUtil.isNotBlank(each.getTrainBrand())) {
                // 分割品牌字符串并转换为整数
                trainBrandSet.addAll(StrUtil.split(each.getTrainBrand(), ",").stream().map(Integer::parseInt).toList());
            }
        }
        // 转换为列表并返回
        return trainBrandSet.stream().toList();
    }

    // 令牌刷新线程池
    private final ScheduledExecutorService tokenIsNullRefreshExecutor = Executors.newScheduledThreadPool(1);

    /**
     * 令牌为空时刷新令牌
     * @param requestParam 购票请求参数
     * @param tokenResult 令牌结果
     */
    private void tokenIsNullRefreshToken(PurchaseTicketReqDTO requestParam, TokenResultDTO tokenResult) {
        // 构建锁键：lock:token:bucket:isnull:{trainId}
        RLock lock = redissonClient.getLock(String.format(LOCK_TOKEN_BUCKET_ISNULL, requestParam.getTrainId()));
        // 尝试获取锁，失败则直接返回
        if (!lock.tryLock()) {
            return;
        }
        // 10秒后执行令牌刷新
        tokenIsNullRefreshExecutor.schedule(() -> {
            try {
                List<Integer> seatTypes = new ArrayList<>();
                Map<Integer, Integer> tokenCountMap = new HashMap<>();
                // 解析令牌为空的座位类型和数量
                tokenResult.getTokenIsNullSeatTypeCounts().stream()
                        .map(each -> each.split("_"))
                        .forEach(split -> {
                            int seatType = Integer.parseInt(split[0]);
                            seatTypes.add(seatType);
                            tokenCountMap.put(seatType, Integer.parseInt(split[1]));
                        });
                // 查询座位类型和数量
                List<SeatTypeCountDTO> seatTypeCountDTOList = seatService.listSeatTypeCount(
                        Long.parseLong(requestParam.getTrainId()),
                        requestParam.getDeparture(),
                        requestParam.getArrival(),
                        seatTypes
                );
                // 检查是否有足够的座位
                for (SeatTypeCountDTO each : seatTypeCountDTOList) {
                    Integer tokenCount = tokenCountMap.get(each.getSeatType());
                    if (tokenCount <= each.getSeatCount()) {
                        // 删除令牌桶中的令牌
                        ticketAvailabilityTokenBucket.delTokenInBucket(requestParam);
                        break;
                    }
                }
            } finally {
                // 释放锁
                lock.unlock();
            }
        }, 10, TimeUnit.SECONDS);
    }

    /**
     * 应用启动时执行的方法
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        // 获取 TicketService 实例，用于后续调用
        ticketService = ApplicationContextHolder.getBean(TicketService.class);
    }
}
