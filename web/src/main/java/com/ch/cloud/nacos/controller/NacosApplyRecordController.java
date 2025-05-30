package com.ch.cloud.nacos.controller;


import com.ch.cloud.devops.manager.INamespaceApplyManager;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ch.cloud.devops.domain.NamespaceApplyRecord;
import com.ch.cloud.devops.service.INamespaceApplyRecordService;
import com.ch.cloud.types.NamespaceType;
import com.ch.e.ExUtils;
import com.ch.e.PubError;
import com.ch.result.InvokerPage;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.s.ApproveStatus;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;


/**
 * <p>
 * 业务-申请记录表 前端控制器
 * </p>
 *
 * @author zhimin.ma
 * @since 2021-11-12
 */
@RestController
@RequestMapping("/nacos/apply")
public class NacosApplyRecordController {
    
    @Autowired
    private INamespaceApplyRecordService namespaceApplyRecordService;
    
    @Autowired
    private INamespaceApplyManager namespaceApplyManager;
    
    @Operation(summary = "Nacos申请分页查询", description = "分页查询命名空间")
    @GetMapping(value = {"/{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<NamespaceApplyRecord> nacosPage(NamespaceApplyRecord record,
            @PathVariable(value = "num") int pageNum, @PathVariable(value = "size") int pageSize) {
        return ResultUtils.wrapPage(() -> {
            record.setType(NamespaceType.NACOS.getCode());
            if (CommonUtils.isEmpty(record.getStatus())) {
                record.setStatus(null);
            }
            PageHelper.orderBy("CREATE_AT desc, ID ASC");
            PageInfo<NamespaceApplyRecord> page = namespaceApplyRecordService.findPage(record, pageNum, pageSize);
            return InvokerPage.build(page.getTotal(), page.getList());
        });
    }
    
    
    @Operation(summary = "审核空间", description = "审核申请命名空间")
    @PostMapping({"/{id:[0-9]+}/approve"})
    public Result<Boolean> approveNacos(@RequestBody NamespaceApplyRecord record) {
        return ResultUtils.wrapFail(() -> {
            NamespaceApplyRecord orig = namespaceApplyRecordService.find(record.getId());
            if (orig == null) {
                ExUtils.throwError(PubError.NOT_EXISTS, record.getId());
            }
            if (NamespaceType.fromCode(orig.getType()) != NamespaceType.NACOS) {
                ExUtils.throwError(PubError.NOT_ALLOWED, "approve type is not [nacos]!");
            }
            if (ApproveStatus.fromValue(record.getStatus()).availableApprove()) {
                ExUtils.throwError(PubError.NOT_ALLOWED, "approve status is not correct!");
            }
            orig.setStatus(record.getStatus());
            orig.setApproveBy(ContextUtil.getUsername());
            orig.setApproveAt(DateUtils.current());
            orig.setUpdateBy(ContextUtil.getUsername());
            orig.setUpdateAt(DateUtils.current());
            return namespaceApplyManager.approveNacos(orig) > 0;
        });
    }
}

