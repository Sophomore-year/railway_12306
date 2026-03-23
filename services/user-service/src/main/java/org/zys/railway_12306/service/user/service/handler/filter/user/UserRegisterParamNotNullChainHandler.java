package org.zys.railway_12306.service.user.service.handler.filter.user;

import org.zys.railway_12306.framework.starter.convention.exception.ClientException;
import org.zys.railway_12306.service.user.enums.UserRegisterErrorCodeEnum;
import org.zys.railway_12306.service.user.pojo.dto.req.UserRegisterReqDTO;

import java.util.Objects;

/**
 *用户注册参数必填检验
 *
 * @author SUM
 * @date 2026/03/16
 */
public class UserRegisterParamNotNullChainHandler implements UserRegisterCreateChainFilter<UserRegisterReqDTO> {
    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        if (Objects.isNull(requestParam.getUsername())) {
            throw new ClientException(UserRegisterErrorCodeEnum.USER_NAME_NOTNULL);
        } else if (Objects.isNull(requestParam.getPassword())) {
            throw new ClientException(UserRegisterErrorCodeEnum.PASSWORD_NOTNULL);
        } else if (Objects.isNull(requestParam.getPhone())) {
            throw new ClientException(UserRegisterErrorCodeEnum.PHONE_NOTNULL);
        } else if (Objects.isNull(requestParam.getIdType())) {
            throw new ClientException(UserRegisterErrorCodeEnum.ID_TYPE_NOTNULL);
        } else if (Objects.isNull(requestParam.getIdCard())) {
            throw new ClientException(UserRegisterErrorCodeEnum.ID_CARD_NOTNULL);
        } else if (Objects.isNull(requestParam.getMail())) {
            throw new ClientException(UserRegisterErrorCodeEnum.MAIL_NOTNULL);
        } else if (Objects.isNull(requestParam.getRealName())) {
            throw new ClientException(UserRegisterErrorCodeEnum.REAL_NAME_NOTNULL);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
