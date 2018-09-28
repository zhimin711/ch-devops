package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.service.ITestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
@RestController
@RequestMapping("test")

@Api("swaggerTestController相关的api")
public class TestCtrl {

    @Autowired
    private ITestService testService;

    @ApiOperation(value = "根据id查询学生信息", notes = "查询数据库中某个的学生信息")
    @ApiImplicitParam(name = "id", value = "学生ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping("save")
    public String save() {
        int c = testService.save("Sys-demo");
        return "========>" + c;
    }
}
