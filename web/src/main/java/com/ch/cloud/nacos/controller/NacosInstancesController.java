package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosInstancesClient;
import com.ch.cloud.nacos.dto.InstanceDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.InstanceVO;
import com.ch.cloud.nacos.vo.InstancesPageVO;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@RequestMapping("/nacos/instances")
public class NacosInstancesController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private NacosInstancesClient nacosInstancesClient;

    @ApiOperation(value = "查询分页", notes = "分页查询服务实例")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<InstanceDTO> instances(InstancesPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<InstancesPageVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosInstancesClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "修改", notes = "修改实例配置")
    @PutMapping
    public Result<Boolean> edit(@RequestBody InstanceVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<InstanceVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosInstancesClient.save(clientEntity);
        });
    }

}
