package org.zys.railway_12306.service.user.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zys.rail_12306.framework.starter.idempotent.annotation.Idempotent;
import org.zys.rail_12306.framework.starter.idempotent.enums.IdempotentSceneEnum;
import org.zys.rail_12306.framework.starter.idempotent.enums.IdempotentTypeEnum;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.user.core.UserContext;
import org.zys.railway_12306.framework.starter.web.Results;
import org.zys.railway_12306.service.user.pojo.dto.req.PassengerRemoveReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.req.PassengerReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.PassengerActualRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.PassengerRespDTO;
import org.zys.railway_12306.service.user.service.PassengerService;

import java.util.List;

/**
 *乘车人控制层
 *
 * @author SUM
 * @date 2026/03/16
 */
@RestController
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    /**
     * 根据用户名查询乘车人列表
     */
    @GetMapping("/api/user-service/passenger/query")
    public Result<List<PassengerRespDTO>> listPassengerQueryByUsername() {
        return Results.success(passengerService.listPassengerQueryByUsername(UserContext.getUsername()));
    }

    /**
     * 根据乘车人 ID 集合查询乘车人列表
     */
    @GetMapping("/api/user-service/inner/passenger/actual/query/ids")
    public Result<List<PassengerActualRespDTO>> listPassengerQueryByIds(@RequestParam("username") String username, @RequestParam("ids") List<Long> ids) {
        return Results.success(passengerService.listPassengerQueryByIds(username, ids));
    }

    /**
     * 新增乘车人
     */
    @Idempotent(
            uniqueKeyPrefix = "railway_12306-user:lock_passenger-alter:",
            key = "T(org.zys.railway_12306.frameworks.starter.user.core.UserContext).getUsername()",
            type = IdempotentTypeEnum.SPEL,
            scene = IdempotentSceneEnum.RESTAPI,
            message = "正在新增乘车人，请稍后再试..."
    )
    @PostMapping("/api/user-service/passenger/save")
    public Result<Void> savePassenger(@RequestBody PassengerReqDTO requestParam) {
        passengerService.savePassenger(requestParam);
        return Results.success();
    }


    /**
     * 修改乘车人
     */
    @Idempotent(
            uniqueKeyPrefix = "railway_12306-user:lock_passenger-alter:",
            key = "T(org.zys.railway_12306.framework.starter.user.core.UserContext).getUsername()",
            type = IdempotentTypeEnum.SPEL,
            scene = IdempotentSceneEnum.RESTAPI,
            message = "正在修改乘车人，请稍后再试..."
    )
    @PostMapping("/api/user-service/passenger/update")
    public Result<Void> updatePassenger(@RequestBody PassengerReqDTO requestParam) {
        passengerService.updatePassenger(requestParam);
        return Results.success();
    }


    /**
     * 移除乘车人
     */
    @PostMapping("/api/user-service/passenger/remove")
    public Result<Void> removePassenger(@RequestBody PassengerRemoveReqDTO requestParam) {
        passengerService.removePassenger(requestParam);
        return Results.success();
    }

}
