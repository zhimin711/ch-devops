package com.ch.cloud.nacos.api;

import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.service.INamespaceService;
import com.ch.cloud.types.NamespaceType;
import com.ch.pojo.VueRecord;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @date 2022/5/14 21:52
 */
@RestController
@RequestMapping("/nacos/namespace")
public class NacosNamespaceAPIController {

    @Autowired
    private INamespaceService namespaceService;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos集群")
    @GetMapping(value = {"list"})
    public Result<VueRecord> list() {
        return ResultUtils.wrapList(()->{
            Namespace record = new Namespace();
            record.setType(NamespaceType.NACOS);
            List<Namespace> list = namespaceService.find(record);
            return VueRecordUtils.covertIdList(list);
        });
    }
}
