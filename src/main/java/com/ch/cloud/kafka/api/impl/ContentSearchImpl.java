package com.ch.cloud.kafka.api.impl;

import com.ch.cloud.kafka.api.IContentSearch;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.pojo.ContentType;
import com.ch.cloud.kafka.pojo.TopicExtInfo;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.TopicExtService;
import com.ch.cloud.kafka.tools.KafkaTool;
import com.ch.err.ErrorCode;
import com.ch.result.BaseResult;
import com.ch.type.Status;
import com.ch.utils.CommonUtils;
import com.ch.utils.JarUtils;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */
@Service
@com.alibaba.dubbo.config.annotation.Service
public class ContentSearchImpl implements IContentSearch {

    @Value("${share.path.libs}")
    private String libsDir;

    @Autowired
    private ClusterConfigService clusterConfigService;
    @Autowired
    private TopicExtService topicExtService;
    //加载过不用重新加载类对象
    private static Map<String, Class<?>> clazzMap = Maps.newConcurrentMap();

    @Override
    public BaseResult<String> search(TopicExtInfo record) {
        BtClusterConfig config = clusterConfigService.findByClusterName(record.getClusterName());
        BtTopicExt topicExt = topicExtService.findByClusterAndTopic(record.getClusterName(), record.getTopicName());

        KafkaTool kafkaTool = new KafkaTool(config.getZookeeper());
        KafkaTool.SearchType searchType = KafkaTool.SearchType.LATEST;
        if ("0".equals(record.getType())) {
            searchType = KafkaTool.SearchType.CONTENT;
            if (CommonUtils.isEmpty(record.getDescription())) {
                return new BaseResult<>(ErrorCode.NON_NULL, "搜索内容不能为空！");
            }
        } else if ("2".equals(record.getType())) {
            searchType = KafkaTool.SearchType.EARLIEST;
        }
        if ((searchType == KafkaTool.SearchType.EARLIEST || searchType == KafkaTool.SearchType.LATEST) && CommonUtils.isNumeric(record.getDescription())) {
            long size = Long.valueOf(record.getDescription());
            if (size > 1000) {
                return new BaseResult<>(ErrorCode.ARGS, "搜索条数不能超过1000！");
            }
        }

        if (ContentType.from(topicExt.getType()) == ContentType.PROTO_STUFF) {
            try {
                Class<?> clazz = clazzMap.get(topicExt.getClassName());
                if (clazz == null) {
                    if (CommonUtils.isEmpty(topicExt.getClassFile())) {
                        clazz = Class.forName(topicExt.getClassName());
                    } else {
                        String prefix = "file:" + libsDir;
                        clazz = JarUtils.loadClassForJar(prefix + File.separator + record.getClassFile(), topicExt.getClassName());
                    }
                    clazzMap.put(topicExt.getClassName(), clazz);
                }
                List<String> records = kafkaTool.searchTopicProtostuffContent(record.getTopicName(), record.getDescription(), clazz, searchType);
                return new BaseResult<>(records);
            } catch (MalformedURLException | ClassNotFoundException e) {
                e.printStackTrace();
                return new BaseResult<>(ErrorCode.ARGS, "序列化类：" + topicExt.getClassName() + "不存在！");
            }
        } else {
            try {
                List<String> records = kafkaTool.searchTopicStringContent(topicExt.getTopicName(), record.getDescription(), searchType);
                return new BaseResult<>(records);
            } catch (Exception ignored) {

            }
        }
        return new BaseResult<>(Status.FAILED);
    }

}
