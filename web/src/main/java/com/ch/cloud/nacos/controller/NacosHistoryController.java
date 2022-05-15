package com.ch.cloud.nacos.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.client.NacosHistoryClient;
import com.ch.cloud.nacos.dto.HistoryDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.AssertUtils;
import com.ch.utils.BeanUtilsV2;
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
@RequestMapping("/nacos/history")
public class NacosHistoryController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private NacosHistoryClient      nacosHistoryClient;
    @Autowired
    private NacosConfigsClient      nacosConfigsClient;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos配置历史")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<HistoryDTO> page(HistoryPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<HistoryPageVO> entity = nacosNamespaceValidator.validConfig(record);
            record.setTenant(record.getNamespaceId());
            return nacosHistoryClient.fetchPage(entity);
        });
    }


    @ApiOperation(value = "查询", notes = "查询配置详情")
    @GetMapping
    public Result<HistoryDTO> get(HistoryQueryVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<HistoryQueryVO> clientEntity = nacosNamespaceValidator.validConfig(record);
            record.setTenant(record.getNamespaceId());
            return nacosHistoryClient.fetch(clientEntity);
        });
    }

    @ApiOperation(value = "查询", notes = "查询配置详情")
    @PutMapping
    public Result<Boolean> rollback(@RequestParam String opType, @RequestBody HistoryRollbackVO record) {
        return ResultUtils.wrapFail(() -> {
            switch (opType) {
                case "I":
                    return delete(record);
                case "D":
                case "U":
                    return save(record);
                default:
                    ExceptionUtils._throw(PubError.NOT_ALLOWED, "非法操作");
            }
            return false;
        });
    }

    private Boolean save(HistoryRollbackVO record) {
        ConfigVO configVO = new ConfigVO();
        BeanUtil.copyProperties(record, configVO);
        ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validConfig(configVO);
        return nacosConfigsClient.add(clientEntity);
    }

    private Boolean delete(HistoryRollbackVO record) {
        ConfigDeleteVO deleteVO = new ConfigDeleteVO();
        BeanUtil.copyProperties(record, deleteVO);
        ClientEntity<ConfigDeleteVO> clientEntity = nacosNamespaceValidator.validConfig(deleteVO);
        return nacosConfigsClient.delete(clientEntity);
    }
}
