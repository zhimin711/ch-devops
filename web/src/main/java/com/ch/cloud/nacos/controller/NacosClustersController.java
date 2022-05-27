package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosClusterClient;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.e.PubError;
import com.ch.pojo.VueRecord;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.AssertUtils;
import com.ch.utils.VueRecordUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * desc:nacos集群Controller
 *
 * @author zhimin
 * @since 2022/4/23 22:44
 */
@RestController
@RequestMapping("/nacos/clusters")
public class NacosClustersController {

    @Autowired
    private INacosClusterService nacosClusterService;
    @Autowired
    private NacosClusterClient   nacosClusterClient;

    @Autowired
    private INamespaceService namespaceService;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos集群")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<NacosCluster> page(NacosCluster record,
                                         @PathVariable(value = "num") int pageNum,
                                         @PathVariable(value = "size") int pageSize) {
        PageInfo<NacosCluster> page = nacosClusterService.findPage(record, pageNum, pageSize);
        return PageResult.success(page.getTotal(), page.getList());
    }

    @ApiOperation(value = "添加", notes = "添加nacos集群")
    @PostMapping
    public Result<Integer> add(@RequestBody NacosCluster record) {
        return ResultUtils.wrapFail(() -> {
            NacosCluster cluster = nacosClusterService.findByUrl(record.getUrl());
            AssertUtils.isTrue(cluster != null, PubError.EXISTS, record.getUrl());
            record.setUrl(record.getUrl().trim());
            return nacosClusterService.save(record);
        });
    }

    @ApiOperation(value = "修改", notes = "修改nacos集群")
    @PutMapping({"{id:[0-9]+}"})
    public Result<Integer> edit(@RequestBody NacosCluster record) {
        record.setUrl(null);
        return ResultUtils.wrapFail(() -> nacosClusterService.update(record));
    }

    @GetMapping({"{id:[0-9]+}"})
    public Result<NacosCluster> find(@PathVariable Long id) {
        Result<NacosCluster> result = ResultUtils.wrapFail(() -> {
            NacosCluster cluster = nacosClusterService.find(id);
            AssertUtils.isEmpty(cluster, PubError.CONFIG, "nacos cluster");
            return cluster;
        });
        if (result.isSuccess()) {
            Object info = nacosClusterClient.fetchNodes(result.get().getUrl());
            result.putExtra("nodes", info);
        }
        return result;
    }

    //    @ApiOperation(value = "删除", notes = "删除nacos集群")
    //@DeleteMapping({"{id:[0-9]+}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> nacosClusterService.delete(id));
    }

    @GetMapping({"{id:[0-9]+}/namespaces"})
    public Result<VueRecord> findNamespaces(@PathVariable Long id, @RequestParam(name = "s", required = false) String name) {
        return ResultUtils.wrapList(() -> {
            List<Namespace> list = namespaceService.findByClusterIdAndName(id, name);
            return VueRecordUtils.covertIdTree(list);
        });
    }

    @GetMapping
    public Result<VueRecord> list() {
        return ResultUtils.wrapList(() -> {
            List<NacosCluster> list = nacosClusterService.findPageList(new NacosCluster(), 1, 100);
            return VueRecordUtils.covertIdTree(list);
        });
    }
}
