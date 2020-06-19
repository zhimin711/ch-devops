package com.ch.cloud.kafka.controller;

import com.ch.Constants;
import com.ch.cloud.kafka.pojo.DubboCall;
import com.ch.cloud.kafka.pojo.DubboCall2;
import com.ch.cloud.kafka.utils.DubboCallUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */

@Api(tags = "Dubbo 服务接口")
@RestController
@RequestMapping("dubbo")
@Slf4j
public class DubboCallController {

    @Value("${share.path.libs}")
    private String libsDir;

/*
    @ApiOperation(value = "Dubbo 调用")
    @PostMapping("call")
    public Object search(@RequestBody DubboCall record,
                            @RequestHeader(Constants.TOKEN_USER) String username) {
        return DubboCallUtils.invoke(record.getInterfaceName(), record.getMethod(), record.getParam(), record.getAddress(), record.getVersion());
    }
*/

    @ApiOperation(value = "Dubbo 调用2")
    @PostMapping("call2")
    public Object call2(@RequestBody DubboCall2 record,
                         @RequestHeader(Constants.TOKEN_USER) String username) {
        return DubboCallUtils.invoke(record.getAddress(), record.getVersion(),record.getInterfaceName(),
                record.getMethod(), record.getParamClassName(),
                record.getParamJson());}
}
