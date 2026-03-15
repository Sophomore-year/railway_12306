package org.zys.railway_12306.service.user.service;


import org.zys.railway_12306.service.user.pojo.dto.req.UserDeletionReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.req.UserLoginReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.req.UserRegisterReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserLoginRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserRegisterRespDTO;

/**
 * 用户登录服务
 */
public interface UserLoginService {

    /**
     * 用户登录
     * @param requestParam 用户登录入参
     * @return 用户登录返回结果
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);


    /**
     *通过 Token 检查用户是否登录
     *
     * @param accessToken 用户登录 Token 凭证
     * @return {@link UserLoginRespDTO }
     */
    UserLoginRespDTO checkLogin(String accessToken);

    /**
     *用户退出登录
     *
     * @param accessToken 用户登录 Token 凭证
     */
    void logout(String accessToken);

    /**
     * 用户注册
     *
     * @param requestParam 用户注册入参
     * @return 用户注册返回结果
     */
    UserRegisterRespDTO register(UserRegisterReqDTO requestParam);

    /**
     * 注销用户
     *
     * @param requestParam 注销用户入参
     */
    void deletion(UserDeletionReqDTO requestParam);

}
