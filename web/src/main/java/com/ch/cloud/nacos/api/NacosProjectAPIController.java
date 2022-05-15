package com.ch.cloud.nacos.api;

import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.pojo.VueRecord;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/14 21:52
 */
@RestController
@RequestMapping("/nacos/projects")
public class NacosProjectAPIController {

    @Autowired
    private UpmsProjectClientService upmsProjectClientService;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos项目")
    @GetMapping(value = {"list"})
    public Result<VueRecord> list(String name, String tenant) {
        return ResultUtils.wrapList(()->{
            Result<ProjectDto> result = upmsProjectClientService.list(name, tenant);
            return VueRecordUtils.covertIdList(result.getRows());
        });
    }
}
