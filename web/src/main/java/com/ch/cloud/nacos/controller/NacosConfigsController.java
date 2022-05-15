package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.service.INamespaceService;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ConfigQueryVO;
import com.ch.cloud.nacos.vo.ConfigVO;
import com.ch.cloud.nacos.vo.ConfigsQueryVO;
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.AssertUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：配置
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@RequestMapping("/nacos/configs")
public class NacosConfigsController {

    @Autowired
    private INamespaceService  namespaceService;
    @Autowired
    private NacosConfigsClient nacosConfigsClient;
    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos配置")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<ConfigDTO> page(ConfigsQueryVO record) {

        return ResultUtils.wrapPage(() -> {
            ClientEntity<ConfigsQueryVO> entity = nacosNamespaceValidator.validConfig(record);
            record.setTenant(record.getNamespaceId());
            record.setNamespaceId(null);
            return nacosConfigsClient.fetchPage(entity);
        });
    }

    @ApiOperation(value = "添加", notes = "添加配置")
    @PostMapping
    public Result<Integer> add(@RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validConfig(record);
            return nacosConfigsClient.add(clientEntity);
        });
    }

    @ApiOperation(value = "查询", notes = "查询配置详情")
    @GetMapping
    public Result<ConfigDTO> get(ConfigQueryVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigQueryVO> clientEntity = nacosNamespaceValidator.validConfig(record);
            record.setTenant(record.getNamespaceId());
            return nacosConfigsClient.fetch(clientEntity);
        });
    }

    @ApiOperation(value = "修改", notes = "修改配置")
    @PutMapping
    public Result<Integer> edit(@RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validConfig(record);
            return nacosConfigsClient.edit(clientEntity);
        });
    }


}
