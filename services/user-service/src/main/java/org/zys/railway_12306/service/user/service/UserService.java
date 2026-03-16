package org.zys.railway_12306.service.user.service;

import jakarta.validation.constraints.NotEmpty;
import org.zys.railway_12306.service.user.pojo.dto.req.UserUpdateReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserQueryActualRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserQueryRespDTO;

/**
 *用户信息接口层
 *
 * @author SUM
 * @date 2026/03/15
 */
public interface UserService {
    /**
     * 根据用户 ID 查询用户信息
     *
     * @param userId 用户 ID
     * @return 用户详细信息
     */
    UserQueryRespDTO queryUserByUserId(@NotEmpty String userId);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户详细信息
     */
    UserQueryRespDTO queryUserByUsername(@NotEmpty String username);

    /**
     * 根据用户名查询用户无脱敏信息
     *
     * @param username 用户名
     * @return 用户详细信息
     */
    UserQueryActualRespDTO queryActualUserByUsername(@NotEmpty String username);

    /**
     * 根据证件类型和证件号查询注销次数
     *
     * @param idType 证件类型
     * @param idCard 证件号
     * @return 注销次数
     */
    Integer queryUserDeletionNum(Integer idType, String idCard);

    /**
     * 根据用户 ID 修改用户信息
     *
     * @param requestParam 用户信息入参
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     *检查用户名是否已存在
     *
     * @param username 用户名
     * @return {@link Boolean }
     */
    Boolean hasUsername(@NotEmpty String username);
}
