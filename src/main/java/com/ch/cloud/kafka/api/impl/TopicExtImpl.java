package com.ch.cloud.kafka.api.impl;

import com.ch.Status;
import com.ch.cloud.kafka.api.ITopicExt;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.pojo.TopicExtInfo;
import com.ch.cloud.kafka.service.TopicExtService;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 01370603
 * @date 2018/9/25 20:29
 */
@Service
@com.alibaba.dubbo.config.annotation.Service
public class TopicExtImpl implements ITopicExt {
    @Autowired
    TopicExtService topicExtService;

    @Override
    public Result<Long> save(TopicExtInfo record) {
        BtTopicExt r = new BtTopicExt();
        BeanUtils.copyProperties(record, r, "id");
        topicExtService.save(r);
        return new Result<>(r.getId());
    }

    @Override
    public Result<Long> update(TopicExtInfo record) {
        BtTopicExt r = new BtTopicExt();
        BeanUtils.copyProperties(record, r);
        topicExtService.update(r);
        return new Result<>(Status.SUCCESS);
    }

    @Override
    public PageResult<TopicExtInfo> findPageBy(int pageNum, int pageSize, TopicExtInfo record) {
        BtTopicExt r = new BtTopicExt();
        BeanUtils.copyProperties(record, r);
        PageInfo<BtTopicExt> page = topicExtService.findPage(r, pageNum, pageSize);
        List<TopicExtInfo> records = page.getList().stream().map(e -> {
            TopicExtInfo info = new TopicExtInfo();
            BeanUtils.copyProperties(e, info);
            return info;
        }).collect(Collectors.toList());
        return new PageResult<>(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Result<TopicExtInfo> findListBy(TopicExtInfo record) {
        BtTopicExt r = new BtTopicExt();
        BeanUtils.copyProperties(record, r);
        List<BtTopicExt> list = topicExtService.find(r);
        List<TopicExtInfo> records = list.stream().map(e -> {
            TopicExtInfo info = new TopicExtInfo();
            BeanUtils.copyProperties(e, info);
            return info;
        }).collect(Collectors.toList());
        return new Result<>(records);
    }

}
