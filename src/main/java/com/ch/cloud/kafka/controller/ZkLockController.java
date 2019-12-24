package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.tools.*;
import com.ch.e.CoreError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 描述：com.ch.cloud.kafka.controller
 *
 * @author zhimin
 * 2018/2/22.
 * @version 1.0
 * @since 1.8
 */
@RestController
@RequestMapping("zk")
@Api(value = "ZkLockController", description = "zookeeper分布式锁接口")
public class ZkLockController {

    @Autowired(required = false)
    LockTool lockTool;
    @Autowired(required = false)
    OrderIdGenerator orderIdGenerator;

    @Autowired
    ILock lock;

    @ApiOperation(value = "分布式锁", notes = "主题搜索接口")
    @PostMapping("/lock")
    public Result<String> lock() {
        return ResultUtils.wrapFail(() -> {
            lock.getLock();
            try {
                return orderIdGenerator.generate();
            } finally {
                lock.unLock();
            }
        });
    }

}
