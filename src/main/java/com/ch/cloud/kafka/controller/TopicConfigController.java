package com.ch.cloud.kafka.controller;

import com.ch.Status;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.pojo.TopicExtInfo;
import com.ch.cloud.kafka.service.TopicExtService;
import com.ch.result.InvokerPage;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 01370603
 * @date 2018/9/25 20:29
 */
@Api(tags = "KAFKA主题配置模块")
@RestController
@RequestMapping("topic")
public class TopicConfigController {

    @Autowired
    TopicExtService topicExtService;

    @ApiOperation(value = "新增扩展信息", notes = "")
    @PostMapping("ext")
    public Result<Long> save(TopicExtInfo record) {
        BtTopicExt r = new BtTopicExt();
        BeanUtils.copyProperties(record, r, "id");
        topicExtService.save(r);
        return Result.success(r.getId());
    }

    @ApiOperation(value = "修改扩展信息", notes = "")
    @PostMapping("ext/{id}")
    public Result<Long> update(@PathVariable Long id, TopicExtInfo record) {
        BtTopicExt r = new BtTopicExt();
        BeanUtils.copyProperties(record, r);
        topicExtService.update(r);
        return new Result<>(Status.SUCCESS);
    }

    @ApiOperation(value = "分页查询", notes = "只需要在请求头中附带token即可，无需任何参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "分页大小", required = true),
            @ApiImplicitParam(name = "record", value = "查询条件", paramType = "query")
    })
    @GetMapping("ext/{pageNum}/{pageSize}")
    public PageResult<TopicExtInfo> findPageBy(@PathVariable int pageNum, @PathVariable int pageSize, TopicExtInfo record) {
        return ResultUtils.wrapPage(() -> {
            BtTopicExt r = new BtTopicExt();
            BeanUtils.copyProperties(record, r);
            PageInfo<BtTopicExt> page = topicExtService.findPage(pageNum, pageSize, r);
            List<TopicExtInfo> records = page.getList().stream().map(e -> {
                TopicExtInfo info = new TopicExtInfo();
                BeanUtils.copyProperties(e, info);
                return info;
            }).collect(Collectors.toList());

            return new InvokerPage.Page<>(page.getTotal(), records);
        });

    }


}
