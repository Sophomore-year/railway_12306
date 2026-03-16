package org.zys.railway_12306.service.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.service.user.mapper.UserDeletionMapper;
import org.zys.railway_12306.service.user.mapper.UserMailMapper;
import org.zys.railway_12306.service.user.mapper.UserMapper;
import org.zys.railway_12306.service.user.pojo.dto.req.UserUpdateReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserQueryActualRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserQueryRespDTO;
import org.zys.railway_12306.service.user.pojo.entity.User;
import org.zys.railway_12306.service.user.pojo.entity.UserDeletion;
import org.zys.railway_12306.service.user.pojo.entity.UserMail;
import org.zys.railway_12306.service.user.service.UserService;

import java.util.Objects;
import java.util.Optional;

import static org.zys.railway_12306.service.user.constant.RedisKeyConstant.USER_REGISTER_REUSE_SHARDING;
import static org.zys.railway_12306.service.user.constant.RedisKeyConstant.USER_DELETION_COUNT_SHARDING;
import static org.zys.railway_12306.service.user.toolkit.UserReuseUtil.hashShardingIdx;

/**
 *用户信息接口实现层
 *
 * @author SUM
 * @date 2026/03/16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final DistributedCache distributedCache;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final UserMailMapper userMailMapper;
    private final UserDeletionMapper userDeletionMapper;

    @Override
    public UserQueryRespDTO queryUserByUserId(String userId) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getId, userId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new ClientException("用户不存在，请检查用户ID是否正确");
        }
        return BeanUtil.convert(user, UserQueryRespDTO.class);
    }

    @Override
    public UserQueryRespDTO queryUserByUsername(String username) {
        //通过用户名查询用户信息
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new ClientException("用户不存在，请检查用户名是否正确");
        }
        //
        return BeanUtil.convert(user, UserQueryRespDTO.class);
    }

    @Override
    public UserQueryActualRespDTO queryActualUserByUsername(String username) {
        return BeanUtil.convert(queryUserByUsername(username), UserQueryActualRespDTO.class);
    }

    @Override
    public Integer queryUserDeletionNum(Integer idType, String idCard) {
        LambdaQueryWrapper<UserDeletion> queryWrapper = Wrappers.lambdaQuery(UserDeletion.class)
                .eq(UserDeletion::getIdType, idType)
                .eq(UserDeletion::getIdCard, idCard);

        // 生成缓存键，使用idType和idCard的组合确保唯一性
        String cacheKey = USER_DELETION_COUNT_SHARDING + hashShardingIdx(idCard + idType);

        // 1. 先从缓存中获取用户删除次数
        Integer deletionCountFromCache = distributedCache.get(cacheKey, Integer.class);
        if (deletionCountFromCache != null) {
            // 2. 缓存中存在数据，直接返回
            return deletionCountFromCache;
        }

        // 3. 缓存中不存在，从数据库查询
        Long deletionCountFromDB = userDeletionMapper.selectCount(queryWrapper);
        Integer deletionCount = Optional.ofNullable(deletionCountFromDB).map(Long::intValue).orElse(0);

        // 4. 将查询结果存入缓存，设置合理的过期时间
        distributedCache.put(cacheKey, deletionCount, 3600L); // 缓存1小时

        return deletionCount;
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // 1. 根据用户名查询用户当前信息
        UserQueryRespDTO userQueryRespDTO = queryUserByUsername(requestParam.getUsername());

        // 2. 将请求参数转换为User实体对象
        User user = BeanUtil.convert(requestParam, User.class);

        // 3. 构建用户更新条件，根据用户名进行更新
        LambdaUpdateWrapper<User> userUpdateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getUsername, requestParam.getUsername());

        // 4. 执行用户信息更新操作
        userMapper.update(user, userUpdateWrapper);

        // 5. 检查邮箱是否需要更新：邮箱不为空且与原邮箱不同
        if (StrUtil.isNotBlank(requestParam.getMail()) && !Objects.equals(requestParam.getMail(), userQueryRespDTO.getMail())) {
            // 6. 构建邮箱删除条件，根据原邮箱进行删除
            LambdaUpdateWrapper<UserMail> updateWrapper = Wrappers.lambdaUpdate(UserMail.class)
                    .eq(UserMail::getMail, userQueryRespDTO.getMail());

            // 7. 删除原邮箱记录
            userMailMapper.delete(updateWrapper);

            // 8. 构建新邮箱对象
            UserMail userMail = UserMail.builder()
                    .mail(requestParam.getMail())
                    .username(requestParam.getUsername())
                    .build();

            // 9. 插入新邮箱记录
            userMailMapper.insert(userMail);
        }
    }

    @Override
    public Boolean hasUsername(String username) {
        //判断布隆过滤器中是否可能包含username元素。
        boolean hasUsername = userRegisterCachePenetrationBloomFilter.contains(username);
        if (hasUsername) {
            //如果可能包含username元素，则从Redis中查询username元素是否存在。
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            return instance.opsForSet().isMember(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        }
        return true;
    }
}
