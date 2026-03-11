package org.zys.railway_12306.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.zys.railway_12306.service.user.pojo.entity.UserPhone;

/**
 *用户手机号持久层
 *
 * @author SUM
 * @date 2026/03/11
 */
public interface UserPhoneMapper extends BaseMapper<UserPhone> {
    /**
     * 注销用户
     *
     * @param userPhone 注销用户入参
     */
    void deletionUser(UserPhone userPhone);
}
