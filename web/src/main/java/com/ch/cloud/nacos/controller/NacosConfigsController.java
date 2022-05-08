package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.client.ClientEntity;
import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.service.INamespaceService;
import com.ch.cloud.nacos.vo.ConfigQueryVO;
import com.ch.cloud.nacos.vo.ConfigVO;
import com.ch.cloud.nacos.vo.ConfigsQueryVO;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
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
@RequestMapping("/nacos/configs")
public class NacosConfigsController {

    @Autowired
    private INamespaceService  namespaceService;
    @Autowired
    private NacosConfigsClient nacosConfigsClient;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos集群")
    @GetMapping
    public PageResult<ConfigDTO> page(ConfigsQueryVO record) {

        return ResultUtils.wrapPage(() -> {
            ExceptionUtils.assertEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
            Namespace namespace = namespaceService.findWithCluster(record.getNamespaceId());
            ExceptionUtils.assertNull(namespace, PubError.NOT_EXISTS, "集群ID：" + record.getNamespaceId());
            record.setNamespaceId(null);
            record.setTenant(namespace.getUid());
            return nacosConfigsClient.fetchPage(new ClientEntity<>(namespace.getAddr(), record));
        });
    }

    @ApiOperation(value = "添加", notes = "添加命名空间")
    @PostMapping
    public Result<Integer> add(@RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
//            checkSaveOrUpdate(record);
            return nacosConfigsClient.add(new ClientEntity<>());
        });
    }

    @ApiOperation(value = "查询", notes = "查询命名空间详情")
    @GetMapping({"{namespaceId:[0-9]+}"})
    public Result<ConfigDTO> get(ConfigQueryVO record) {
        return ResultUtils.wrapFail(() -> {
            ExceptionUtils.assertEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
            Namespace namespace = namespaceService.findWithCluster(record.getNamespaceId());
            ExceptionUtils.assertNull(namespace, PubError.NOT_EXISTS, "集群ID：" + record.getNamespaceId());
            record.setNamespaceId(namespace.getUid());
            record.setTenant(namespace.getUid());
            return nacosConfigsClient.fetch(new ClientEntity<>(namespace.getAddr(), record));
        });
    }

    @ApiOperation(value = "修改", notes = "修改命名空间")
    @PutMapping({"{id:[0-9]+}"})
    public Result<Integer> edit(@RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
//            checkSaveOrUpdate(record);
            return nacosConfigsClient.edit(new ClientEntity<>());
        });
    }

}
