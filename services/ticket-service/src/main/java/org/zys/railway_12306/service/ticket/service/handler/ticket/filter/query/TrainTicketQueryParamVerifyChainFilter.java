package org.zys.railway_12306.service.ticket.service.handler.ticket.filter.query;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.service.ticket.mapper.RegionMapper;
import org.zys.railway_12306.service.ticket.mapper.StationMapper;
import org.zys.railway_12306.service.ticket.pojo.dto.req.TicketPageQueryReqDTO;
import org.zys.railway_12306.service.ticket.pojo.entity.Region;
import org.zys.railway_12306.service.ticket.pojo.entity.Station;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.LOCK_QUERY_ALL_REGION_LIST;
import static org.zys.railway_12306.service.ticket.constant.RedisKeyConstant.QUERY_ALL_REGION_LIST;

/**
 * 查询列车车票流程过滤器之验证数据是否正确
 *
 * @author SUM
 * @date 2026/04/11
 */
@Component
@RequiredArgsConstructor
public class TrainTicketQueryParamVerifyChainFilter implements TrainTicketQueryChainFilter<TicketPageQueryReqDTO> {

    private final RegionMapper regionMapper;
    private final StationMapper stationMapper;
    private final DistributedCache distributedCache;
    private final RedissonClient redissonClient;

    /**
     * 缓存数据为空并且已经加载过标识
     */
    private static boolean CACHE_DATA_ISNULL_AND_LOAD_FLAG = false;

    @Override
    public void handler(TicketPageQueryReqDTO requestParam) {
        // 1. 获取 StringRedisTemplate 实例，用于操作 Redis
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        
        // 2. 获取 HashOperations 实例，用于操作 Redis Hash 数据结构
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        
        // 3. 从 Redis 缓存中查询出发站和到达站是否存在
        // QUERY_ALL_REGION_LIST 是缓存键，存储了所有地区和站点的信息
        List<Object> actualExistList = hashOperations.multiGet(
                QUERY_ALL_REGION_LIST,
                ListUtil.toList(requestParam.getFromStation(), requestParam.getToStation())
        );
        
        // 4. 统计不存在的站点数量（值为 null 表示不存在）
        long emptyCount = actualExistList.stream().filter(Objects::isNull).count();
        
        // 5. 如果两个站点都存在，直接返回
        if (emptyCount == 0L) {
            return;
        }
        
        // 6. 如果有一个站点不存在，或者缓存已加载但两个站点都不存在，抛出异常
        if (emptyCount == 1L || (emptyCount == 2L && CACHE_DATA_ISNULL_AND_LOAD_FLAG && distributedCache.hasKey(QUERY_ALL_REGION_LIST))) {
            throw new ClientException("出发地或目的地不存在");
        }
        
        // 7. 获取分布式锁，防止并发加载缓存
        RLock lock = redissonClient.getLock(LOCK_QUERY_ALL_REGION_LIST);
        lock.lock();
        
        try {
            // 8. 再次检查缓存是否存在（双重检查锁定模式）
            if (distributedCache.hasKey(QUERY_ALL_REGION_LIST)) {
                // 8.1 如果缓存存在，再次查询出发站和到达站是否存在
                actualExistList = hashOperations.multiGet(
                        QUERY_ALL_REGION_LIST,
                        ListUtil.toList(requestParam.getFromStation(), requestParam.getToStation())
                );
                
                // 8.2 统计存在的站点数量
                emptyCount = actualExistList.stream().filter(Objects::nonNull).count();
                
                // 8.3 如果不是两个站点都存在，抛出异常
                if (emptyCount != 2L) {
                    throw new ClientException("出发地或目的地不存在");
                }
                
                // 8.4 如果两个站点都存在，直接返回
                return;
            }
            
            // 9. 如果缓存不存在，从数据库查询所有地区和站点信息
            List<Region> regionList = regionMapper.selectList(Wrappers.emptyWrapper());
            List<Station> stationList = stationMapper.selectList(Wrappers.emptyWrapper());
            
            // 10. 构建地区和站点的映射关系
            HashMap<Object, Object> regionValueMap = Maps.newHashMap();
            for (Region each : regionList) {
                regionValueMap.put(each.getCode(), each.getName());
            }
            for (Station each : stationList) {
                regionValueMap.put(each.getCode(), each.getName());
            }
            
            // 11. 将地区和站点信息存储到 Redis 缓存中
            hashOperations.putAll(QUERY_ALL_REGION_LIST, regionValueMap);
            
            // 12. 标记缓存已加载
            CACHE_DATA_ISNULL_AND_LOAD_FLAG = true;
            
            // 13. 检查出发站和到达站是否存在于映射关系中
            emptyCount = regionValueMap.keySet().stream()
                    .filter(each -> StrUtil.equalsAny(each.toString(), requestParam.getFromStation(), requestParam.getToStation()))
                    .count();
            
            // 14. 如果不是两个站点都存在，抛出异常
            if (emptyCount != 2L) {
                throw new ClientException("出发地或目的地不存在");
            }
        } finally {
            // 15. 释放分布式锁
            lock.unlock();
        }
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
