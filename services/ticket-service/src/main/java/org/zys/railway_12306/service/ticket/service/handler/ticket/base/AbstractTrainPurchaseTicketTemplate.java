package org.zys.railway_12306.service.ticket.service.handler.ticket.base;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.zys.rail_12306.framework.starter.bases.ApplicationContextHolder;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.rail_12306.framework.starter.designpattern.strategy.AbstractExecuteStrategy;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.RouteDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.TrainSeatBaseDTO;
import org.zys.railway_12306.service.ticket.service.TrainStationService;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.SelectSeatDTO;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;

import java.util.List;

import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.TRAIN_STATION_REMAINING_TICKET;

/**
 * 抽象高铁购票模板基础服务
 * <p>
 * 该类是一个抽象模板类，定义了高铁购票的基本流程和通用逻辑
 * 实现了 IPurchaseTicket、CommandLineRunner 和 AbstractExecuteStrategy 接口
 * 使用模板方法模式，将座位选择逻辑留给子类实现
 * </p>
 *
 * @author SUM
 * @date 2026/04/14
 */
public abstract class AbstractTrainPurchaseTicketTemplate implements IPurchaseTicket, CommandLineRunner, AbstractExecuteStrategy<SelectSeatDTO, List<TrainPurchaseTicketRespDTO>> {

    /**
     * 分布式缓存实例，用于操作 Redis 缓存
     */
    private DistributedCache distributedCache;
    
    /**
     * 余票缓存更新类型，用于判断是否需要更新缓存
     */
    private String ticketAvailabilityCacheUpdateType;
    
    /**
     * 车站服务实例，用于获取车站相关信息
     */
    private TrainStationService trainStationService;

    /**
     * 选择座位
     * <p>
     * 抽象方法，由子类实现具体的座位选择逻辑
     * </p>
     *
     * @param requestParam 购票请求入参，包含列车信息、乘客信息等
     * @return 乘车人座位信息列表
     */
    protected abstract List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam);

    /**
     * 构建 TrainSeatBaseDTO 对象
     * <p>
     * 根据购票请求参数构建座位基础信息对象
     * </p>
     *
     * @param requestParam 购票请求入参
     * @return 座位基础信息对象
     */
    protected TrainSeatBaseDTO buildTrainSeatBaseDTO(SelectSeatDTO requestParam) {
        return TrainSeatBaseDTO.builder()
                .trainId(requestParam.getRequestParam().getTrainId())            // 设置列车ID
                .departure(requestParam.getRequestParam().getDeparture())        // 设置出发站
                .arrival(requestParam.getRequestParam().getArrival())            // 设置到达站
                .chooseSeatList(requestParam.getRequestParam().getChooseSeats())  // 设置选择的座位列表
                .passengerSeatDetails(requestParam.getPassengerSeatDetails())    // 设置乘客座位详情
                .build();
    }

    /**
     * 执行购票策略，带返回值
     * <p>
     * 实现 AbstractExecuteStrategy 接口的方法，执行座位选择并更新余票缓存
     * </p>
     *
     * @param requestParam 购票请求入参
     * @return 乘车人座位信息列表
     */
    @Override
    public List<TrainPurchaseTicketRespDTO> executeResp(SelectSeatDTO requestParam) {
        // 调用子类实现的 selectSeats 方法选择座位
        List<TrainPurchaseTicketRespDTO> actualResult = selectSeats(requestParam);
        
        // 扣减车厢余票缓存，扣减站点余票缓存
        // 条件：座位分配成功且余票缓存更新类型不是 "binlog"
        if (CollUtil.isNotEmpty(actualResult) && !StrUtil.equals(ticketAvailabilityCacheUpdateType, "binlog")) {
            // 获取列车ID、出发站、到达站
            String trainId = requestParam.getRequestParam().getTrainId();
            String departure = requestParam.getRequestParam().getDeparture();
            String arrival = requestParam.getRequestParam().getArrival();
            
            // 获取 Redis 模板实例
            StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
            
            // 获取列车站点路径列表
            List<RouteDTO> routeDTOList = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
            
            // 遍历路径列表，更新每个站点区间的余票缓存
            routeDTOList.forEach(each -> {
                // 构建缓存键后缀：列车ID_起始站_终点站
                String keySuffix = StrUtil.join("_", trainId, each.getStartStation(), each.getEndStation());
                // 更新余票缓存，扣减实际购票数量
                stringRedisTemplate.opsForHash().increment(
                        TRAIN_STATION_REMAINING_TICKET + keySuffix,  // 缓存键
                        String.valueOf(requestParam.getSeatType()),    // 座位类型
                        -actualResult.size()                           // 扣减数量
                );
            });
        }
        
        // 返回座位分配结果
        return actualResult;
    }

    /**
     * 应用启动时执行的初始化方法
     * <p>
     * 实现 CommandLineRunner 接口的方法，在应用启动时初始化必要的服务实例和配置
     * </p>
     *
     * @param args 命令行参数
     * @throws Exception 初始化过程中的异常
     */
    @Override
    public void run(String... args) throws Exception {
        // 使用 ApplicationContextHolder 获取分布式缓存实例
        distributedCache = ApplicationContextHolder.getBean(DistributedCache.class);
        
        // 使用 ApplicationContextHolder 获取车站服务实例
        trainStationService = ApplicationContextHolder.getBean(TrainStationService.class);
        
        // 使用 ApplicationContextHolder 获取配置环境实例
        ConfigurableEnvironment configurableEnvironment = ApplicationContextHolder.getBean(ConfigurableEnvironment.class);
        
        // 从配置环境中获取余票缓存更新类型，默认为空字符串
        ticketAvailabilityCacheUpdateType = configurableEnvironment.getProperty("ticket.availability.cache-update.type", "");
    }
}
