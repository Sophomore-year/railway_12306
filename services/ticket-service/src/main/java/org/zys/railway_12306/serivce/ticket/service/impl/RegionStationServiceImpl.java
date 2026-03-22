package org.zys.railway_12306.serivce.ticket.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.rail_12306.framework.starter.cache.core.CacheLoader;
import org.zys.rail_12306.framework.starter.cache.toolkit.CacheUtil;
import org.zys.railway_12306.framework.starter.common.enums.FlagEnum;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.serivce.ticket.enums.RegionStationQueryTypeEnum;
import org.zys.railway_12306.serivce.ticket.mapper.RegionMapper;
import org.zys.railway_12306.serivce.ticket.mapper.StationMapper;
import org.zys.railway_12306.serivce.ticket.pojo.dto.req.RegionStationQueryReqDTO;
import org.zys.railway_12306.serivce.ticket.pojo.dto.resp.RegionStationQueryRespDTO;
import org.zys.railway_12306.serivce.ticket.pojo.dto.resp.StationQueryRespDTO;
import org.zys.railway_12306.serivce.ticket.pojo.entity.Region;
import org.zys.railway_12306.serivce.ticket.pojo.entity.Station;
import org.zys.railway_12306.serivce.ticket.service.RegionStationService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.zys.railway_12306.serivce.ticket.constant.Railway12306Constant.ADVANCE_TICKET_DAY;
import static org.zys.railway_12306.serivce.ticket.constant.RedisKeyConstant.LOCK_QUERY_REGION_STATION_LIST;
import static org.zys.railway_12306.serivce.ticket.constant.RedisKeyConstant.REGION_STATION;
import static org.zys.railway_12306.serivce.ticket.constant.RedisKeyConstant.STATION_ALL;

/**
 *地区以及车站接口实现层
 *
 * @author SUM
 * @date 2026/03/22
 */
@Service
@RequiredArgsConstructor
public class RegionStationServiceImpl implements RegionStationService {

    private final RegionMapper regionMapper;
    private final StationMapper stationMapper;
    private final DistributedCache distributedCache;
    private final RedissonClient redissonClient;

    @Override
    public List<RegionStationQueryRespDTO> listRegionStation(RegionStationQueryReqDTO requestParam) {
        String key;
        // 1. 如果请求参数中包含名称，则按名称查询车站
        if (StrUtil.isNotBlank(requestParam.getName())) {
            // 2. 构建缓存键
            key  = REGION_STATION  + requestParam.getName();
            // 3. 调用安全获取区域车站的方法
            return safeGetRegionStation(
                    key ,
                    // 4. 定义缓存加载器，查询名称或拼音匹配的车站
                    () -> {
                        LambdaQueryWrapper<Station> queryWrapper = Wrappers.lambdaQuery(Station.class)
                                .likeRight(Station::getName, requestParam.getName())  // 按车站名称前缀匹配
                                .or()
                                .likeRight(Station::getSpell, requestParam.getName());  // 按车站拼音前缀匹配
                        List<Station> stationList = stationMapper.selectList(queryWrapper);
                        // 5. 将查询结果转换为DTO并序列化为JSON字符串
                        return JSON.toJSONString(BeanUtil.convert(stationList, RegionStationQueryRespDTO.class));
                    },
                    requestParam.getName()  // 传递参数用于构建锁键
            );
        }
        // 6. 如果请求参数中包含查询类型，则按类型查询区域
        key  = REGION_STATION  + requestParam.getQueryType();
        // 7. 根据查询类型构建查询条件
        LambdaQueryWrapper<Region> queryWrapper = switch (requestParam.getQueryType()) {
            case 0 -> Wrappers.lambdaQuery(Region.class)
                    .eq(Region::getPopularFlag, FlagEnum.TRUE.code());  // 查询热门区域
            case 1 -> Wrappers.lambdaQuery(Region.class)
                    .in(Region::getInitial, RegionStationQueryTypeEnum.A_E.getSpells());  // 查询A-E开头的区域
            case 2 -> Wrappers.lambdaQuery(Region.class)
                    .in(Region::getInitial, RegionStationQueryTypeEnum.F_J.getSpells());  // 查询F-J开头的区域
            case 3 -> Wrappers.lambdaQuery(Region.class)
                    .in(Region::getInitial, RegionStationQueryTypeEnum.K_O.getSpells());  // 查询K-O开头的区域
            case 4 -> Wrappers.lambdaQuery(Region.class)
                    .in(Region::getInitial, RegionStationQueryTypeEnum.P_T.getSpells());  // 查询P-T开头的区域
            case 5 -> Wrappers.lambdaQuery(Region.class)
                    .in(Region::getInitial, RegionStationQueryTypeEnum.U_Z.getSpells());  // 查询U-Z开头的区域
            default -> throw new ClientException("查询失败，请检查查询参数是否正确");  // 无效查询类型
        };
        // 8. 调用安全获取区域车站的方法
        return safeGetRegionStation(
                key,
                // 9. 定义缓存加载器，查询符合条件的区域
                () -> {
                    List<Region> regionList = regionMapper.selectList(queryWrapper);
                    // 10. 将查询结果转换为DTO并序列化为JSON字符串
                    return JSON.toJSONString(BeanUtil.convert(regionList, RegionStationQueryRespDTO.class));
                },
                String.valueOf(requestParam.getQueryType())  // 传递参数用于构建锁键
        );
    }

    @Override
    public List<StationQueryRespDTO> listAllStation() {
        return distributedCache.safeGet(
                STATION_ALL,
                List.class,
                () -> BeanUtil.convert(stationMapper.selectList(Wrappers.emptyWrapper()), StationQueryRespDTO.class),
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS
        );
    }

    /**
     * 安全获取区域车站信息，包含缓存检查和分布式锁处理
     * @param key 缓存键
     * @param loader 缓存加载器
     * @param param 参数，用于构建锁键
     * @return 区域车站列表
     */
    private  List<RegionStationQueryRespDTO> safeGetRegionStation(final String key, CacheLoader<String> loader, String param) {
        List<RegionStationQueryRespDTO> result;
        // 1. 尝试从缓存中获取数据
        if (CollUtil.isNotEmpty(result = JSON.parseArray(distributedCache.get(key, String.class), RegionStationQueryRespDTO.class))) {
            return result;  // 缓存命中，直接返回
        }

        // 2. 缓存未命中，构建分布式锁键
        // 如：lock_query_region_station_list:北京
        // 如：lock_query_region_station_list:1
        String lockKey = String.format(LOCK_QUERY_REGION_STATION_LIST, param);
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();  // 获取分布式锁

        try {
            // 3. 再次检查缓存，防止缓存穿透
            if (CollUtil.isEmpty(result = JSON.parseArray(distributedCache.get(key, String.class), RegionStationQueryRespDTO.class))) {
                // 4. 缓存仍未命中，加载数据并设置缓存
                if (CollUtil.isEmpty(result = loadAndSet(key, loader))) {
                    return Collections.emptyList();  // 加载失败，返回空列表
                }
            }
        } finally {
            lock.unlock();  // 释放分布式锁
        }

        return result;
    }

    /**
     * 加载数据并设置缓存
     * @param key 缓存键
     * @param loader 缓存加载器
     * @return 区域车站列表
     */
    private List<RegionStationQueryRespDTO> loadAndSet(final String key, CacheLoader<String> loader) {
        // 1. 调用加载器加载数据
        String result = loader.load();

        // 2. 检查加载结果是否为空
        if (CacheUtil.isNullOrBlank(result)) {
            return Collections.emptyList();  // 加载结果为空，返回空列表
        }

        // 3. 解析JSON字符串为DTO列表
        List<RegionStationQueryRespDTO> respDTOList = JSON.parseArray(result, RegionStationQueryRespDTO.class);

        // 4. 将结果存入缓存，设置过期时间为预售天数
        distributedCache.put(
                key,
                result,
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS
        );

        return respDTOList;
    }
}
