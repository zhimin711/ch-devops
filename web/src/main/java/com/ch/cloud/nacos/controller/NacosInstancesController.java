package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosInstancesClient;
import com.ch.cloud.nacos.dto.InstanceDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.InstanceClientVO;
import com.ch.cloud.nacos.vo.InstancesPageClientVO;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Tag(name = "Nacos配置实例服务")
@RestController
@RequestMapping("/nacos/instances")
public class NacosInstancesController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private NacosInstancesClient nacosInstancesClient;

    @Operation(summary = "分页查询", description = "分页查询服务实例")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<InstanceDTO> instances(InstancesPageClientVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<InstancesPageClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosInstancesClient.fetchPage(clientEntity);
        });
    }

    @Operation(summary = "修改", description = "修改实例配置")
    @PutMapping
    public Result<Boolean> edit(@RequestBody InstanceClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<InstanceClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosInstancesClient.save(clientEntity);
        });
    }
}
