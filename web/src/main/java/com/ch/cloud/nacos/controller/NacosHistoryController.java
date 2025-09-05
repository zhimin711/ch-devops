package com.ch.cloud.nacos.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.client.NacosHistoryClient;
import com.ch.cloud.nacos.dto.HistoryDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.e.ExUtils;
import com.ch.e.PubError;
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
@Tag(name = "Nacos历史记录服务")
@RestController
@RequestMapping("/nacos/history")
public class NacosHistoryController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private NacosHistoryClient      nacosHistoryClient;
    @Autowired
    private NacosConfigsClient      nacosConfigsClient;

    @Operation(summary = "分页查询", description = "分页查询nacos配置历史")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<HistoryDTO> page(HistoryPageClientVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<HistoryPageClientVO> entity = nacosNamespaceValidator.valid(record);
            record.setTenant(record.getNamespaceId());
            return nacosHistoryClient.fetchPage(entity);
        });
    }

    @Operation(summary = "查询", description = "查询配置详情")
    @GetMapping
    public Result<HistoryDTO> get(HistoryQueryClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<HistoryQueryClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            record.setTenant(record.getNamespaceId());
            return nacosHistoryClient.fetch(clientEntity);
        });
    }

    @Operation(summary = "回滚", description = "根据操作类型回滚配置")
    @PutMapping
    public Result<Boolean> rollback(@RequestParam String opType, @RequestBody HistoryRollbackClientVO record) {
        return ResultUtils.wrapFail(() -> {
            switch (opType) {
                case "I":
                    return delete(record);
                case "D":
                case "U":
                    return save(record);
                default:
                    ExUtils.throwError(PubError.NOT_ALLOWED, "非法操作");
            }
            return false;
        });
    }

    private Boolean save(HistoryRollbackClientVO record) {
        ConfigClientVO configVO = new ConfigClientVO();
        BeanUtil.copyProperties(record, configVO);
        ClientEntity<ConfigClientVO> clientEntity = nacosNamespaceValidator.valid(configVO);
        return nacosConfigsClient.add(clientEntity);
    }

    private Boolean delete(HistoryRollbackClientVO record) {
        ConfigDeleteClientVO deleteVO = new ConfigDeleteClientVO();
        BeanUtil.copyProperties(record, deleteVO);
        ClientEntity<ConfigDeleteClientVO> clientEntity = nacosNamespaceValidator.valid(deleteVO);
        return nacosConfigsClient.delete(clientEntity);
    }
}