package com.ch.cloud.nacos.controller;

import com.ch.cloud.client.UpmsProjectClientService;
import com.ch.cloud.client.dto.ProjectDto;
import com.ch.result.PageResult;
import com.ch.result.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：Nacos项目服务
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@Api(tags = "Nacos项目服务")
@RequestMapping("/nacos/projects")
public class NacosProjectsController {

    @Autowired
    private UpmsProjectClientService upmsProjectClientService;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos集群")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<ProjectDto> page(ProjectDto record,
                                       @PathVariable(value = "num") int pageNum,
                                       @PathVariable(value = "size") int pageSize) {
//        PageInfo<ProjectDto> page = nacosClusterService.findPage(record, pageNum, pageSize);

        return ResultUtils.wrapPage(() -> null);
    }
}
