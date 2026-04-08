package org.zys.railway_12306.service.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.framework.starter.user.core.UserContext;
import org.zys.railway_12306.service.user.enums.VerifyStatusEnum;
import org.zys.railway_12306.service.user.mapper.PassengerMapper;
import org.zys.railway_12306.service.user.pojo.dto.req.PassengerRemoveReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.req.PassengerReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.PassengerActualRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.PassengerRespDTO;
import org.zys.railway_12306.service.user.pojo.entity.Passenger;
import org.zys.railway_12306.service.user.service.PassengerService;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.zys.railway_12306.service.user.constant.RedisKeyConstant.USER_PASSENGER_LIST;

/**
 *乘车人接口实现层
 *
 * @author SUM
 * @date 2026/03/16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerServiceImpl implements PassengerService {

    private final PassengerMapper passengerMapper;
    private final DistributedCache distributedCache;

    private String getActualUserPassengerListStr(String username) {
        return distributedCache.safeGet(
                USER_PASSENGER_LIST + username,
                String.class,
                () -> {
                    LambdaQueryWrapper<Passenger> queryWrapper = Wrappers.lambdaQuery(Passenger.class)
                            .eq(Passenger::getUsername, username);
                    List<Passenger> passengerList = passengerMapper.selectList(queryWrapper);
                    return CollUtil.isNotEmpty(passengerList) ? JSON.toJSONString(passengerList) : null;
                },
                1,
                TimeUnit.DAYS
        );
    }


    @Override
    public List<PassengerRespDTO> listPassengerQueryByUsername(String username) {
        // 1. 获取用户乘车人列表的JSON字符串（从缓存或数据库）
        String actualUserPassengerListStr = getActualUserPassengerListStr(username);

        // 2. 使用Optional包装字符串，处理可能为null的情况
        return Optional.ofNullable(actualUserPassengerListStr)
                // 3. 将JSON字符串解析为Passenger对象列表
                .map(each -> JSON.parseArray(each, Passenger.class))
                // 4. 将Passenger对象列表转换为PassengerRespDTO对象列表
                .map(each -> BeanUtil.convert(each, PassengerRespDTO.class))
                // 5. 如果原始字符串为null，返回null
                .orElse(null);
    }

    @Override
    public List<PassengerActualRespDTO> listPassengerQueryByIds(String username, List<Long> ids) {
        // 1. 获取用户乘车人列表的JSON字符串（从缓存或数据库）
        String actualUserPassengerListStr = getActualUserPassengerListStr(username);

        // 2. 检查字符串是否为空，如果为空则返回null
        if (StrUtil.isEmpty(actualUserPassengerListStr)) {
            return null;
        }

        // 3. 将JSON字符串解析为Passenger对象列表
        // 4. 使用Stream API过滤出ID在指定列表中的乘车人
        // 5. 将过滤后的Passenger对象转换为PassengerActualRespDTO对象
        // 6. 收集结果并返回
        return JSON.parseArray(actualUserPassengerListStr, Passenger.class)
                .stream().filter(passenger -> ids.contains(passenger.getId()))
                .map(each -> BeanUtil.convert(each, PassengerActualRespDTO.class))
                .collect(Collectors.toList());
    }

    private void verifyPassenger(PassengerReqDTO requestParam) {
        int length = requestParam.getRealName().length();
        if (!(length >= 2 && length <= 16)) {
            throw new ClientException("乘车人名称请设置2-16位的长度");
        }
        if (!IdcardUtil.isValidCard(requestParam.getIdCard())) {
            throw new ClientException("乘车人证件号错误");
        }
        if (!PhoneUtil.isMobile(requestParam.getPhone())) {
            throw new ClientException("乘车人手机号错误");
        }
    }

    private void delUserPassengerCache(String username) {
        distributedCache.delete(USER_PASSENGER_LIST + username);
    }

    @Override
    public void savePassenger(PassengerReqDTO requestParam) {
        // 1. 验证乘车人请求参数的合法性
        verifyPassenger(requestParam);

        // 2. 从上下文获取当前登录的用户名
        String username = UserContext.getUsername();

        try {
            // 3. 将请求参数转换为Passenger实体对象
            Passenger passenger = BeanUtil.convert(requestParam, Passenger.class);

            // 4. 设置乘车人关联的用户名
            passenger.setUsername(username);

            // 5. 设置创建日期为当前时间
            passenger.setCreateDate(new Date());

            // 6. 设置验证状态为已审核
            passenger.setVerifyStatus(VerifyStatusEnum.REVIEWED.getCode());

            // 7. 插入乘车人信息到数据库
            int inserted = passengerMapper.insert(passenger);

            // 8. 检查插入是否成功
            if (!SqlHelper.retBool(inserted)) {
                throw new ServiceException(String.format("[%s] 新增乘车人失败", username));
            }
        } catch (Exception ex) {
            // 9. 异常处理
            if (ex instanceof ServiceException) {
                // 9.1 记录业务异常日志
                log.error("{}，请求参数：{}", ex.getMessage(), JSON.toJSONString(requestParam));
            } else {
                // 9.2 记录其他异常日志，包括堆栈信息
                log.error("[{}] 新增乘车人失败，请求参数：{}", username, JSON.toJSONString(requestParam), ex);
            }
            // 10. 重新抛出异常，确保上层能够感知到错误
            throw ex;
        }

        // 11. 删除用户乘车人缓存，确保下次查询时获取最新数据
        delUserPassengerCache(username);
    }

    @Override
    public void updatePassenger(PassengerReqDTO requestParam) {
        // 1. 验证乘车人请求参数的合法性
        verifyPassenger(requestParam);

        // 2. 从上下文获取当前登录的用户名
        String username = UserContext.getUsername();

        try {
            // 3. 将请求参数转换为Passenger实体对象
            Passenger passenger = BeanUtil.convert(requestParam, Passenger.class);

            // 4. 设置乘车人关联的用户名
            passenger.setUsername(username);

            // 5. 构建更新条件，确保只更新当前用户的指定ID的乘车人信息
            LambdaUpdateWrapper<Passenger> updateWrapper = Wrappers.lambdaUpdate(Passenger.class)
                    .eq(Passenger::getUsername, username)
                    .eq(Passenger::getId, requestParam.getId());
            // 6. 执行更新操作
            int updated = passengerMapper.update(passenger, updateWrapper);

            // 7. 检查更新是否成功
            if (!SqlHelper.retBool(updated)) {
                throw new ServiceException(String.format("[%s] 修改乘车人失败", username));
            }
        } catch (Exception ex) {
            // 8. 异常处理
            if (ex instanceof ServiceException) {
                // 8.1 记录业务异常日志
                log.error("{}，请求参数：{}", ex.getMessage(), JSON.toJSONString(requestParam));
            } else {
                // 8.2 记录其他异常日志，包括堆栈信息
                log.error("[{}] 修改乘车人失败，请求参数：{}", username, JSON.toJSONString(requestParam), ex);
            }
            // 9. 重新抛出异常，确保上层能够感知到错误
            throw ex;
        }

        // 10. 删除用户乘车人缓存，确保下次查询时获取最新数据
        delUserPassengerCache(username);
    }


    private Passenger selectPassenger(String username, String passengerId) {
        LambdaQueryWrapper<Passenger> queryWrapper = Wrappers.lambdaQuery(Passenger.class)
                .eq(Passenger::getUsername, username)
                .eq(Passenger::getId, passengerId);
        return passengerMapper.selectOne(queryWrapper);
    }


    @Override
    public void removePassenger(PassengerRemoveReqDTO requestParam) {
        String username = UserContext.getUsername();
        Passenger passenger = selectPassenger(username, requestParam.getId());
        if (Objects.isNull(passenger)) {
            throw new ClientException("乘车人数据不存在");
        }
        try {
            LambdaUpdateWrapper<Passenger> deleteWrapper = Wrappers.lambdaUpdate(Passenger.class)
                    .eq(Passenger::getUsername, username)
                    .eq(Passenger::getId, requestParam.getId());
            // 逻辑删除，修改数据库表记录 del_flag
            int deleted = passengerMapper.delete(deleteWrapper);
            if (!SqlHelper.retBool(deleted)) {
                throw new ServiceException(String.format("[%s] 删除乘车人失败", username));
            }
        } catch (Exception ex) {
            if (ex instanceof ServiceException) {
                log.error("{}，请求参数：{}", ex.getMessage(), JSON.toJSONString(requestParam));
            } else {
                log.error("[{}] 删除乘车人失败，请求参数：{}", username, JSON.toJSONString(requestParam), ex);
            }
            throw ex;
        }
        delUserPassengerCache(username);
    }


}
