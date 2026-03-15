package org.zys.railway_12306.service.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zys.rail_12306.framework.starter.cache.DistributedCache;
import org.zys.rail_12306.framework.starter.designpattern.chain.AbstractChainContext;
import org.zys.railway_12306.framework.starter.common.toolkit.BeanUtil;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.framework.starter.convention.exception.ServiceException;
import org.zys.railway_12306.framework.starter.user.core.UserContext;
import org.zys.railway_12306.framework.starter.user.core.UserInfoDTO;
import org.zys.railway_12306.framework.starter.user.toolkit.JWTUtil;
import org.zys.railway_12306.service.user.enums.UserChainMarkEnum;
import org.zys.railway_12306.service.user.mapper.UserDeletionMapper;
import org.zys.railway_12306.service.user.mapper.UserMailMapper;
import org.zys.railway_12306.service.user.mapper.UserMapper;
import org.zys.railway_12306.service.user.mapper.UserPhoneMapper;
import org.zys.railway_12306.service.user.mapper.UserReuseMapper;
import org.zys.railway_12306.service.user.pojo.dto.req.UserDeletionReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.req.UserLoginReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.req.UserRegisterReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserLoginRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserQueryRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserRegisterRespDTO;
import org.zys.railway_12306.service.user.pojo.entity.User;
import org.zys.railway_12306.service.user.pojo.entity.UserDeletion;
import org.zys.railway_12306.service.user.pojo.entity.UserMail;
import org.zys.railway_12306.service.user.pojo.entity.UserPhone;
import org.zys.railway_12306.service.user.pojo.entity.UserReuse;
import org.zys.railway_12306.service.user.service.UserLoginService;
import org.zys.railway_12306.service.user.service.UserService;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.zys.railway_12306.service.user.constant.RedisKeyConstant.LOCK_USER_REGISTER;
import static org.zys.railway_12306.service.user.constant.RedisKeyConstant.USER_DELETION;
import static org.zys.railway_12306.service.user.constant.RedisKeyConstant.USER_REGISTER_REUSE_SHARDING;
import static org.zys.railway_12306.service.user.enums.UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL;
import static org.zys.railway_12306.service.user.enums.UserRegisterErrorCodeEnum.MAIL_REGISTERED;
import static org.zys.railway_12306.service.user.enums.UserRegisterErrorCodeEnum.PHONE_REGISTERED;
import static org.zys.railway_12306.service.user.enums.UserRegisterErrorCodeEnum.USER_REGISTER_FAIL;
import static org.zys.railway_12306.service.user.toolkit.UserReuseUtil.hashShardingIdx;


/**
 *
 *
 * @author SUM
 * @date 2026/03/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final UserMapper userMapper;
    private final UserPhoneMapper userPhoneMapper;
    private final UserMailMapper userMailMapper;
    private final DistributedCache distributedCache;
    private final AbstractChainContext<UserRegisterReqDTO> abstractChainContext;
    private final RedissonClient redissonClient;
    private final UserReuseMapper userReuseMapper;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final UserService userService;
    private final UserDeletionMapper userDeletionMapper;
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {

        String usernameOrMailOrPhone = requestParam.getUsernameOrMailOrPhone();
        // 判断是否是邮箱格式
        boolean mailFlag = false;
        for (char c : usernameOrMailOrPhone.toCharArray()) {
            if (c == '@') {
                mailFlag = true;
                break;
            }
        }
        String username;

        if (mailFlag) {
            //通过邮箱查找对应的用户名。
            LambdaQueryWrapper<UserMail> queryWrapper = Wrappers.lambdaQuery(UserMail.class)
                    .eq(UserMail::getMail, usernameOrMailOrPhone);
            username = Optional.ofNullable(userMailMapper.selectOne(queryWrapper))
                    .map(UserMail::getUsername)
                    .orElseThrow(() -> new ClientException("用户名/手机号/邮箱不存在"));
        } else {
            LambdaQueryWrapper<UserPhone> queryWrapper = Wrappers.lambdaQuery(UserPhone.class)
                    .eq(UserPhone::getPhone, usernameOrMailOrPhone);
            username = Optional.ofNullable(userPhoneMapper.selectOne(queryWrapper))
                    .map(UserPhone::getUsername)
                    .orElse(null);
        }
        username = Optional.ofNullable(username).orElse(requestParam.getUsernameOrMailOrPhone());
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
                .eq(User::getPassword, requestParam.getPassword())
                .select(User::getId, User::getUsername, User::getRealName);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .userId(String.valueOf(user.getId()))
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .build();
            String accessToken = JWTUtil.generateAccessToken(userInfo);
            UserLoginRespDTO userLogin = UserLoginRespDTO.builder()
                    .userId(userInfo.getUserId())
                    .username(userInfo.getUsername())
                    .realName(userInfo.getRealName())
                    .accessToken(accessToken)
                    .build();
            //缓存用户登录信息（30分钟）
            distributedCache.put(accessToken, JSON.toJSONString(userLogin), 30, TimeUnit.MINUTES);
            return userLogin;
        }
        throw new ServiceException("账号不存在或密码错误");
    }

    @Override
    public UserLoginRespDTO checkLogin(String accessToken) {
        return distributedCache.get(accessToken, UserLoginRespDTO.class);
    }

    @Override
    public void logout(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)) {
            distributedCache.delete(accessToken);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterRespDTO register(UserRegisterReqDTO requestParam) {
        // 1. 调用链式处理上下文，执行用户注册过滤逻辑
        abstractChainContext.handler(UserChainMarkEnum.USER_REGISTER_FILTER.name(), requestParam);
        
        // 2. 获取分布式锁，防止同一用户名并发注册
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER + requestParam.getUsername());
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            // 3. 如果获取锁失败，说明该用户名正在注册中，抛出异常
            throw new ServiceException(HAS_USERNAME_NOTNULL);
        }
        
        try {
            // 4. 插入用户基本信息到数据库
            try {
                int inserted = userMapper.insert(BeanUtil.convert(requestParam, User.class));
                if (inserted < 1) {
                    // 5. 插入失败，抛出注册失败异常
                    throw new ServiceException(USER_REGISTER_FAIL);
                }
            } catch (DuplicateKeyException dke) {
                // 6. 捕获唯一键冲突异常，说明用户名已存在
                log.error("用户名 [{}] 重复注册", requestParam.getUsername());
                throw new ServiceException(HAS_USERNAME_NOTNULL);
            }
            
            // 7. 构建用户手机号关联信息
            UserPhone userPhone = UserPhone.builder()
                    .phone(requestParam.getPhone())
                    .username(requestParam.getUsername())
                    .build();
            
            // 8. 插入用户手机号到数据库
            try {
                userPhoneMapper.insert(userPhone);
            } catch (DuplicateKeyException dke) {
                // 9. 捕获唯一键冲突异常，说明手机号已被注册
                log.error("用户 [{}] 注册手机号 [{}] 重复", requestParam.getUsername(), requestParam.getPhone());
                throw new ServiceException(PHONE_REGISTERED);
            }
            
            // 10. 如果邮箱不为空，处理邮箱信息
            if (StrUtil.isNotBlank(requestParam.getMail())) {
                // 11. 构建用户邮箱关联信息
                UserMail userMail = UserMail.builder()
                        .mail(requestParam.getMail())
                        .username(requestParam.getUsername())
                        .build();
                
                // 12. 插入用户邮箱到数据库
                try {
                    userMailMapper.insert(userMail);
                } catch (DuplicateKeyException dke) {
                    // 13. 捕获唯一键冲突异常，说明邮箱已被注册
                    log.error("用户 [{}] 注册邮箱 [{}] 重复", requestParam.getUsername(), requestParam.getMail());
                    throw new ServiceException(MAIL_REGISTERED);
                }
            }
            
            // 14. 获取用户名
            String username = requestParam.getUsername();
            
            // 15. 从用户复用表中删除对应记录（如果存在）
            userReuseMapper.delete(Wrappers.update(new UserReuse(username)));
            
            // 16. 从Redis集合中移除用户名，清理注册复用缓存
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            instance.opsForSet().remove(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
            
            // 17. 将用户名添加到布隆过滤器，防止缓存穿透
            userRegisterCachePenetrationBloomFilter.add(username);
        } finally {
            // 18. 无论注册成功与否，都释放分布式锁
            lock.unlock();
        }
        
        // 19. 将请求参数转换为响应DTO并返回
        return BeanUtil.convert(requestParam, UserRegisterRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletion(UserDeletionReqDTO requestParam) {
        // 1. 从上下文获取当前登录的用户名
        String username = UserContext.getUsername();
        
        // 2. 验证注销请求中的用户名是否与当前登录用户名一致
        if (!Objects.equals(username, requestParam.getUsername())) {
            // 3. 不一致时抛出异常，需要上报风控中心进行异常检测
            throw new ClientException("注销账号与登录账号不一致");
        }
        
        // 4. 获取分布式锁，防止并发注销操作
        RLock lock = redissonClient.getLock(USER_DELETION + requestParam.getUsername());
        lock.lock();
        
        try {
            // 5. 根据用户名查询用户信息
            UserQueryRespDTO userQueryRespDTO = userService.queryUserByUsername(username);
            
            // 6. 构建用户注销记录对象
            UserDeletion userDeletion = UserDeletion.builder()
                    .idType(userQueryRespDTO.getIdType())
                    .idCard(userQueryRespDTO.getIdCard())
                    .build();
            
            // 7. 插入用户注销记录到数据库
            userDeletionMapper.insert(userDeletion);
            
            // 8. 构建用户对象，设置删除时间
            User userDO = new User();
            userDO.setDeletionTime(System.currentTimeMillis());
            userDO.setUsername(username);
            
            // 9. 执行用户删除操作（自定义SQL，因为MyBatis Plus不支持修改del_flag字段）
            // MyBatis Plus 不支持修改语句变更 del_flag 字段
            userMapper.deletionUser(userDO);
            
            // 10. 构建用户手机号对象，设置删除时间
            UserPhone userPhoneDO = UserPhone.builder()
                    .phone(userQueryRespDTO.getPhone())
                    .deletionTime(System.currentTimeMillis())
                    .build();
            
            // 11. 执行用户手机号删除操作
            userPhoneMapper.deletionUser(userPhoneDO);
            
            // 12. 如果用户有邮箱，处理邮箱信息
            if (StrUtil.isNotBlank(userQueryRespDTO.getMail())) {
                // 13. 构建用户邮箱对象，设置删除时间
                UserMail userMailDO = UserMail.builder()
                        .mail(userQueryRespDTO.getMail())
                        .deletionTime(System.currentTimeMillis())
                        .build();
                
                // 14. 执行用户邮箱删除操作
                userMailMapper.deletionUser(userMailDO);
            }
            
            // 15. 删除用户的token缓存，使其登录状态失效
            distributedCache.delete(UserContext.getToken());
            
            // 16. 将用户名添加到用户复用表
            userReuseMapper.insert(new UserReuse(username));
            
            // 17. 将用户名添加到Redis集合中，用于注册复用
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            instance.opsForSet().add(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        } finally {
            // 18. 无论注销成功与否，都释放分布式锁
            lock.unlock();
        }
    }
}
