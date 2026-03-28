package org.zys.railway_12306.service.order.remote;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zys.railway_12306.framework.starter.convention.result.Result;
import org.zys.railway_12306.service.order.remote.dto.UserQueryActualRespDTO;

//todo 用FeignClient做远程调用
public interface UserRemoteService {

    /**
     * 根据乘车人 ID 集合查询乘车人列表
     */
    @GetMapping("/api/user-service/actual/query")
    Result<UserQueryActualRespDTO> queryActualUserByUsername(@RequestParam("username") @NotEmpty String username);
}
