package org.zys.railway_12306.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.zys.railway_12306.service.user.pojo.entity.User;

/**
 *用户信息持久层
 *
 * @author SUM
 * @date 2026/03/11
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 注销用户
     *
      * @param user 注销用户入参
     */
    void deletionUser(User user);
}
