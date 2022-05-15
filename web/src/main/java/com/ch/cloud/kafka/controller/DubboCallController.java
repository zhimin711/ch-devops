package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.pojo.DubboCall2;
import com.ch.cloud.kafka.utils.DubboCallUtils;
import com.ch.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhimin.ma
 * @since 2018/9/25 10:02
 */

@Api(tags = "Dubbo 服务接口")
@RestController
@RequestMapping("dubbo")
@Slf4j
public class DubboCallController {


    @ApiOperation(value = "Dubbo 调用")
    @PostMapping("call")
    public Object call(@RequestBody DubboCall2 record) {
        return DubboCallUtils.invoke(record.getAddress(), record.getVersion(), record.getInterfaceName(),
                record.getMethod(), record.getParamClassName(),
                record.getParamJson());
    }

    @ApiOperation(value = "Dubbo 存储调用参数")
    @PostMapping("params")
    public Object saveCallParams(@RequestBody DubboCall2 record) {
        return DubboCallUtils.invoke(record.getAddress(), record.getVersion(), record.getInterfaceName(),
                record.getMethod(), record.getParamClassName(),
                record.getParamJson());
    }

    @ApiOperation(value = "Dubbo 更新存储调用参数")
    @PutMapping("params")
    public Object editCallParams(@RequestBody DubboCall2 record) {
        return DubboCallUtils.invoke(record.getAddress(), record.getVersion(), record.getInterfaceName(),
                record.getMethod(), record.getParamClassName(),
                record.getParamJson());
    }


    @ApiOperation(value = "分页查询", notes = "需要在请求头中附带token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "num", value = "页码", required = true),
            @ApiImplicitParam(name = "size", value = "分页大小", required = true),
            @ApiImplicitParam(name = "record", value = "查询条件", paramType = "query")
    })
    @GetMapping(value = {"{num}/{size}"})
    public Result<DubboCall2> page(DubboCall2 record,
                                   @PathVariable(value = "num") int pageNum,
                                   @PathVariable(value = "size") int pageSize) {

        return null;

    }
}
