package com.ch.cloud.nacos.controller;


import com.ch.cloud.devops.domain.NamespaceApplyRecord;
import com.ch.cloud.devops.service.INamespaceApplyRecordService;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.utils.ContextUtil;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.result.InvokerPage;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.s.ApproveStatus;
import com.ch.utils.DateUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "Nacos申请分页查询", notes = "分页查询命名空间")
    @GetMapping(value = {"/{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<NamespaceApplyRecord> nacosPage(NamespaceApplyRecord record,
                                                      @PathVariable(value = "num") int pageNum,
                                                      @PathVariable(value = "size") int pageSize) {
        return ResultUtils.wrapPage(() -> {
            record.setType(NamespaceType.NACOS.getCode());
            PageInfo<NamespaceApplyRecord> page = namespaceApplyRecordService.findPage(record, pageNum, pageSize);
            return InvokerPage.build(page.getTotal(), page.getList());
        });
    }


    @ApiOperation(value = "审核空间", notes = "审核申请命名空间")
    @PostMapping({"/{id:[0-9]+}/approve"})
    public Result<Boolean> approveNacos(@RequestBody NamespaceApplyRecord record) {
        return ResultUtils.wrapFail(() -> {
            NamespaceApplyRecord orig = namespaceApplyRecordService.find(record.getId());
            if (orig == null) ExceptionUtils._throw(PubError.NOT_EXISTS, record.getId());
            if (NamespaceType.fromCode(orig.getType()) != NamespaceType.NACOS) {
                ExceptionUtils._throw(PubError.NOT_ALLOWED, "approve type is not [nacos]!");
            }
            if (ApproveStatus.fromValue(record.getStatus()).availableApprove()) {
                ExceptionUtils._throw(PubError.NOT_ALLOWED, "approve status is not correct!");
            }
            orig.setStatus(record.getStatus());
            orig.setApproveBy(ContextUtil.getUser());
            orig.setApproveAt(DateUtils.current());
            orig.setUpdateBy(ContextUtil.getUser());
            orig.setUpdateAt(DateUtils.current());
            return namespaceApplyRecordService.approveNacos(orig) > 0;
        });
    }
}

