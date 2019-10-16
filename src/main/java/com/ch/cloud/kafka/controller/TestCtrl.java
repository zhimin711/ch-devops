package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.service.ITestService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TestCtrl {

//    @Autowired
    ITestService testService;

    @RequestMapping("save")
    public String save() {
        int c = testService.save("Sys-demo");
        return "========>" + c;
    }
}
