package org.zys.railway_12306.service.ticket.service.handler.ticket.tokenbucket;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.zys.rail_12306.framework.starter.bases.Singleton;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.railway_12306.framework.starter.common.toolkit.Assert;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.service.ticket.enums.VehicleTypeEnum;
import org.zys.railway_12306.service.ticket.mapper.TrainMapper;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.PurchaseTicketPassengerDetailDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.RouteDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.domain.SeatTypeCountDTO;
import org.zys.railway_12306.service.ticket.pojo.dto.req.PurchaseTicketReqDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.Train;
import org.zys.railway_12306.service.ticket.service.SeatService;
import org.zys.railway_12306.service.ticket.service.TrainStationService;
import org.zys.railway_12306.service.ticket.service.handler.ticket.dto.TokenResultDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.zys.railway_12306.service.ticket.constant.Railway12306Constant.ADVANCE_TICKET_DAY;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.LOCK_TICKET_AVAILABILITY_TOKEN_BUCKET;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.TICKET_AVAILABILITY_TOKEN_BUCKET;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.TRAIN_INFO;

/**
 *列车车票余量令牌桶，应对海量并发场景下满足并行、限流以及防超卖等场景
 *
 * @author SUM
 * @date 2026/03/22
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TicketAvailabilityTokenBucket {

    private final DistributedCache distributedCache;
    private final TrainMapper trainMapper;
    private final TrainStationService trainStationService;
    private final RedissonClient redissonClient;
    private final SeatService seatService;

    private static final String LUA_TICKET_AVAILABILITY_TOKEN_BUCKET_PATH = "lua/ticket_availability_token_bucket.lua";


    /**
     * 获取车站间令牌桶中的令牌访问
     * 如果返回 {@link Boolean#TRUE} 代表可以参与接下来的购票下单流程
     * 如果返回 {@link Boolean#FALSE} 代表当前访问出发站点和到达站点令牌已被拿完，无法参与购票下单等逻辑
     *
     * @param requestParam 购票请求参数入参
     * @return 是否获取列车车票余量令牌桶中的令牌返回结果
     */
    public TokenResultDTO takeTokenFromBucket(PurchaseTicketReqDTO requestParam) {
        Train train = distributedCache.safeGet(
                TRAIN_INFO + requestParam.getTrainId(),
                Train.class,
                () -> trainMapper.selectById(requestParam.getTrainId()),
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS);
        List<RouteDTO> routeDTOList = trainStationService
                .listTrainStationRoute(requestParam.getTrainId(), train.getStartStation(), train.getEndStation());
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        String tokenBucketHashKey = TICKET_AVAILABILITY_TOKEN_BUCKET + requestParam.getTrainId();
        Boolean hasKey = distributedCache.hasKey(tokenBucketHashKey);
        if (!hasKey) {
            RLock lock = redissonClient.getLock(String.format(LOCK_TICKET_AVAILABILITY_TOKEN_BUCKET, requestParam.getTrainId()));
            if (!lock.tryLock()) {
                throw new ServiceException("购票异常，请稍候再试");
            }
            try {
                Boolean hasKeyTwo = distributedCache.hasKey(tokenBucketHashKey);
                if (!hasKeyTwo) {
                    List<Integer> seatTypes = VehicleTypeEnum.findSeatTypesByCode(train.getTrainType());
                    Map<String, String> ticketAvailabilityTokenMap = new HashMap<>();
                    for (RouteDTO each : routeDTOList) {
                        List<SeatTypeCountDTO> seatTypeCountDTOList = seatService.listSeatTypeCount(Long.parseLong(requestParam.getTrainId()), each.getStartStation(), each.getEndStation(), seatTypes);
                        for (SeatTypeCountDTO eachSeatTypeCountDTO : seatTypeCountDTOList) {
                            String buildCacheKey = StrUtil.join("_", each.getStartStation(), each.getEndStation(), eachSeatTypeCountDTO.getSeatType());
                            ticketAvailabilityTokenMap.put(buildCacheKey, String.valueOf(eachSeatTypeCountDTO.getSeatCount()));
                        }
                    }
                    stringRedisTemplate.opsForHash().putAll(TICKET_AVAILABILITY_TOKEN_BUCKET + requestParam.getTrainId(), ticketAvailabilityTokenMap);
                }
            } finally {
                lock.unlock();
            }
        }
        DefaultRedisScript<String> actual = Singleton.get(LUA_TICKET_AVAILABILITY_TOKEN_BUCKET_PATH, () -> {
            DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_TICKET_AVAILABILITY_TOKEN_BUCKET_PATH)));
            redisScript.setResultType(String.class);
            return redisScript;
        });
        Assert.notNull(actual);
        Map<Integer, Long> seatTypeCountMap = requestParam.getPassengers().stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType, Collectors.counting()));
        JSONArray seatTypeCountArray = seatTypeCountMap.entrySet().stream()
                .map(entry -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("seatType", String.valueOf(entry.getKey()));
                    jsonObject.put("count", String.valueOf(entry.getValue()));
                    return jsonObject;
                })
                .collect(Collectors.toCollection(JSONArray::new));
        List<RouteDTO> takeoutRouteDTOList = trainStationService
                .listTakeoutTrainStationRoute(requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival());
        String luaScriptKey = StrUtil.join("_", requestParam.getDeparture(), requestParam.getArrival());
        String resultStr = stringRedisTemplate.execute(actual, Lists.newArrayList(tokenBucketHashKey, luaScriptKey), JSON.toJSONString(seatTypeCountArray), JSON.toJSONString(takeoutRouteDTOList));
        TokenResultDTO result = JSON.parseObject(resultStr, TokenResultDTO.class);
        return result == null
                ? TokenResultDTO.builder().tokenIsNull(Boolean.TRUE).build()
                : result;
    }

    /**
     * 删除令牌，一般在令牌与数据库不一致情况下触发
     *
     * @param requestParam 删除令牌容器参数
     */
    public void delTokenInBucket(PurchaseTicketReqDTO requestParam) {
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        String tokenBucketHashKey = TICKET_AVAILABILITY_TOKEN_BUCKET + requestParam.getTrainId();
        stringRedisTemplate.delete(tokenBucketHashKey);
    }
}
