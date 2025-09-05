package com.ch.cloud.nacos.api;

import com.ch.cloud.upms.client.UpmsProjectClient;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.pojo.VueRecord;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private UpmsProjectClient upmsProjectClient;

    @Operation(summary = "分页查询", description = "分页查询nacos项目")
    @GetMapping(value = {"list"})
    public Result<VueRecord> list(@RequestParam(value = "name", required = false) String name,
                                  @RequestParam(value = "code", required = false) String code,
                                  @RequestParam(value = "tenant", required = false) String tenant) {
        return ResultUtils.wrapList(() -> {
            Result<ProjectDto> result = upmsProjectClient.list(name, code, tenant);
            return VueRecordUtils.covertIdList(result.getRows());
        });
    }
}
