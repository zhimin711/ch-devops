package com.ch.cloud.kafka.controller;

import com.ch.Constants;
import com.ch.cloud.kafka.pojo.DubboCall2;
import com.ch.cloud.kafka.utils.DubboCallUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhimin.ma
 * @date 2018/9/25 10:02
 */

@Api(tags = "Dubbo 服务接口")
@RestController
@RequestMapping("dubbo")
@Slf4j
public class DubboCallController {


    @ApiOperation(value = "Dubbo 调用")
    @PostMapping("call")
    public Object call(@RequestBody DubboCall2 record,
                       @RequestHeader(Constants.TOKEN_USER) String username) {
        return DubboCallUtils.invoke(record.getAddress(), record.getVersion(), record.getInterfaceName(),
                record.getMethod(), record.getParamClassName(),
                record.getParamJson());
    }
}
