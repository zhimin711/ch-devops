package com.ch.cloud.nacos.api;

import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.types.NamespaceType;
import com.ch.pojo.VueRecord;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/14 21:52
 */
@RestController
@RequestMapping("/nacos/namespaces")
public class NacosNamespaceAPIController {

    @Autowired
    private INamespaceService namespaceService;

    @Operation(summary = "分页查询", description = "nacos空间列表")
    @GetMapping(value = {"list"})
    public Result<VueRecord> list(@RequestParam Long clusterId) {
        return ResultUtils.wrapList(()->{
            Namespace record = new Namespace();
            record.setClusterId(clusterId);
            record.setType(NamespaceType.NACOS);
            List<Namespace> list = namespaceService.find(record);
            return VueRecordUtils.covertIdList(list);
        });
    }
}
