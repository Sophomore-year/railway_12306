package org.zys.railway_12306.service.user.service.handler.filter.user;

import lombok.AllArgsConstructor;
import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.service.user.enums.UserRegisterErrorCodeEnum;
import org.zys.railway_12306.service.user.pojo.dto.req.UserRegisterReqDTO;
import org.zys.railway_12306.service.user.service.UserService;

/**
 *用户注册用户名唯一检验
 *
 * @author SUM
 * @date 2026/03/16
 */
@AllArgsConstructor
public class UserRegisterHasUsernameChainHandler implements UserRegisterCreateChainFilter<UserRegisterReqDTO> {
    private final UserService userService;

    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        if (!userService.hasUsername(requestParam.getUsername())) {
            throw new ClientException(UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
