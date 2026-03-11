package org.zys.railway_12306.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.zys.railway_12306.service.user.pojo.entity.UserMail;

/**
 *用户信息持久层
 *
 * @author SUM
 * @date 2026/03/11
 */
public interface UserMailMapper extends BaseMapper<UserMail> {
    /**
     * 注销用户
     *
      * @param userMail 注销用户入参
     */
    void deletionUser(UserMail userMail);
}
