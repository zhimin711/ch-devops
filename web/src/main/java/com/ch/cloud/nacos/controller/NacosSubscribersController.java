package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosSubscribesClient;
import com.ch.cloud.nacos.dto.SubscriberDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.SubscribesPageClientVO;
import com.ch.result.PageResult;
import com.ch.result.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Tag(name = "Nacos订阅服务")
@RestController
@RequestMapping("/nacos/subscribers")
public class NacosSubscribersController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private NacosSubscribesClient   nacosSubscribesClient;


    @Operation(summary = "分页查询", description = "分页查询命名空间")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<SubscriberDTO> page(SubscribesPageClientVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<SubscribesPageClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosSubscribesClient.fetchPage(clientEntity);
        });
    }
}
