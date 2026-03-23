package org.zys.railway_12306.service.user.service.handler.filter.user;

import org.zys.rail_12306.framework.starter.designpattern.chain.AbstractChainHandler;
import org.zys.railway_12306.service.user.enums.UserChainMarkEnum;
import org.zys.railway_12306.service.user.pojo.dto.req.UserRegisterReqDTO;

/**
 *用户注册责任链过滤器
 *
 * @author SUM
 * @date 2026/03/16
 */
public interface UserRegisterCreateChainFilter<T extends UserRegisterReqDTO> extends AbstractChainHandler<UserRegisterReqDTO> {
    @Override
    default String mark() {
        return UserChainMarkEnum.USER_REGISTER_FILTER.name();
    }
}
