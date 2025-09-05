package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.pojo.DubboCall2;
import com.ch.cloud.kafka.utils.DubboCallUtils;
import com.ch.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhimin.ma
 * @since 2018/9/25 10:02
 */

@Tag(name = "Dubbo 服务接口")
@RestController
@RequestMapping("dubbo")
@Slf4j
public class DubboCallController {


    @Operation(summary = "Dubbo 调用")
    @PostMapping("call")
    public Object call(@RequestBody DubboCall2 record) {
        return DubboCallUtils.invoke(record.getAddress(), record.getVersion(), record.getInterfaceName(),
                record.getMethod(), record.getParamClassName(),
                record.getParamJson());
    }
    
    @Operation(summary = "Dubbo 存储调用参数")
    @PostMapping("params")
    public Object saveCallParams(@RequestBody DubboCall2 record) {
        return DubboCallUtils.invoke(record.getAddress(), record.getVersion(), record.getInterfaceName(),
                record.getMethod(), record.getParamClassName(),
                record.getParamJson());
    }

    @Operation(summary = "Dubbo 更新存储调用参数")
    @PutMapping("params")
    public Object editCallParams(@RequestBody DubboCall2 record) {
        return DubboCallUtils.invoke(record.getAddress(), record.getVersion(), record.getInterfaceName(),
                record.getMethod(), record.getParamClassName(),
                record.getParamJson());
    }


    @Operation(summary = "分页查询", description = "需要在请求头中附带token")
    @Parameters({
            @Parameter(name = "num", description = "页码", required = true),
            @Parameter(name = "size", description = "分页大小", required = true),
            @Parameter(name = "record", description = "查询条件", in = ParameterIn.QUERY)
    })
    @GetMapping(value = {"{num}/{size}"})
    public Result<DubboCall2> page(DubboCall2 record,
                                   @PathVariable(value = "num") int pageNum,
                                   @PathVariable(value = "size") int pageSize) {

        return null;

    }
}
