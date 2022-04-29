package com.ch.cloud.nacos.controller;

import com.ch.Constants;
import com.ch.cloud.nacos.client.NacosServicesClient;
import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.mapper.INacosMapper;
import com.ch.cloud.nacos.service.INamespaceService;
import com.ch.cloud.nacos.vo.PageServicesVO;
import com.ch.cloud.nacos.vo.ServicesQueryVO;
import com.ch.result.PageResult;
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
@RequestMapping("/nacos/provider")
public class NacosProviderController {

    @Autowired
    private NacosServicesClient nacosServicesClient;

    @Autowired
    private INamespaceService namespaceService;

    @ApiOperation(value = "分页查询", notes = "分页查询命名空间")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<?> page(PageServicesVO record,
                              @RequestHeader(value = Constants.X_TOKEN_USER, required = false) String userId,
                              @PathVariable(value = "num") int pageNum,
                              @PathVariable(value = "size") int pageSize) {
        return ResultUtils.wrapPage(() -> {
            Namespace namespace = namespaceService.findAuth(record.getNamespaceId(), userId);
            ServicesQueryVO queryVO = INacosMapper.INSTANCE.toServicesQuery(record);
            queryVO.setPageNo(pageNum);
            queryVO.setPageSize(pageSize);
            return nacosServicesClient.fetchPage("", queryVO);
        });
    }

}
