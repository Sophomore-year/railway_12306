package org.zys.railway_12306.service.user.service;

import org.zys.railway_12306.service.user.pojo.dto.req.PassengerRemoveReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.req.PassengerReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.PassengerActualRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.PassengerRespDTO;

import java.util.List;

/**
 *乘车人接口层
 *
 * @author SUM
 * @date 2026/03/16
 */
public interface PassengerService {

    /**
     * 根据用户名查询用户乘车人信息
     *
     * @param username 用户名
     * @return 乘车人信息
     */
    List<PassengerRespDTO> listPassengerQueryByUsername(String username);

    /**
     * 根据用户名查询用户乘车人信息
     *
     * @param username 用户名
     * @param ids      乘车人id
     * @return 乘车人信息
     */
    List<PassengerActualRespDTO> listPassengerQueryByIds(String username, List<Long> ids);

    /**
     * 新增乘车人
     *
     * @param requestParam 新增乘车人参数
     */
    void savePassenger(PassengerReqDTO requestParam);

    /**
     * 修改乘车人信息
     *
     * @param requestParam 修改乘车人参数
     */
    void updatePassenger(PassengerReqDTO requestParam);

    /**
     * 删除乘车人
     *
     * @param requestParam 删除乘车人参数
     */
    void removePassenger(PassengerRemoveReqDTO requestParam);
}
