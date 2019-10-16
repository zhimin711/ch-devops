package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.service.ITestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：com.ch.cloud.kafka.controller
 *
 * @author zhimin
 * 2018/2/22.
 * @version 1.0
 * @since 1.8
 */
@Api(value="/test", tags="测试接口模块")
@RestController
@RequestMapping("test")
public class TestController {

//    @Autowired
    ITestService testService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "configName", value = "策略匹配url", required = true),
            @ApiImplicitParam(name = "configUrl", value = "策略匹配url", required = true),
            @ApiImplicitParam(name = "assetsId", value = "资产ID", required = true)
    })
    @ApiOperation(value = "保存", notes = "")
    @PostMapping("save")
    public String save() {
        int c = testService.save("Sys-demo");
        return "========>" + c;
    }
}
