package org.zys.railway_12306.service.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.framework.starter.web.Results;
import org.zys.railway_12306.service.user.pojo.dto.req.UserUpdateReqDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserQueryActualRespDTO;
import org.zys.railway_12306.service.user.pojo.dto.resp.UserQueryRespDTO;
import org.zys.railway_12306.service.user.service.UserService;

/**
 *用户控制层
 *
 * @author SUM
 * @date 2026/03/16
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/user-service/query")
    public Result<UserQueryRespDTO> queryUserByUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(userService.queryUserByUsername(username));
    }

    /**
     * 根据用户名查询用户无脱敏信息
     */
    @GetMapping("/api/user-service/query")
    public Result<UserQueryActualRespDTO> queryActualUserByUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(userService.queryActualUserByUsername(username));
    }

    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/api/user-service/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 修改用户
     */
    @PostMapping("/api/user-service/update")
    public Result<Void> update(@RequestBody @Valid UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Results.success();
    }
}
