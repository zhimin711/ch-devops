package com.ch.cloud.kafka.controller;

import com.ch.Status;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.pojo.ContentQuery;
import com.ch.cloud.kafka.pojo.ContentType;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.TopicExtService;
import com.ch.cloud.kafka.tools.KafkaTool;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.utils.CommonUtils;
import com.ch.utils.JarUtils;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */
@RestController
@RequestMapping("content")
public class ContentSearchController {

    private Logger logger = LoggerFactory.getLogger(ContentSearchController.class);

    @Value("${share.path.libs}")
    private String libsDir;

    @Autowired
    private ClusterConfigService clusterConfigService;
    @Autowired
    private TopicExtService topicExtService;
    //加载过不用重新加载类对象
    private static Map<String, Class<?>> clazzMap = Maps.newConcurrentMap();

    @GetMapping("search")
    public Result<String> search(ContentQuery record) {
        BtClusterConfig config = clusterConfigService.findByClusterName(record.getCluster());
        BtTopicExt topicExt = topicExtService.findByClusterAndTopic(record.getCluster(), record.getTopic());

        KafkaTool kafkaTool = new KafkaTool(config.getZookeeper());
        KafkaTool.SearchType searchType = KafkaTool.SearchType.LATEST;
        if ("0".equals(record.getType())) {
            searchType = KafkaTool.SearchType.CONTENT;
            if (CommonUtils.isEmpty(record.getContent())) {
                return Result.error(PubError.NON_NULL, "全量搜索，内容不能为空！");
            }
        } else if ("2".equals(record.getType())) {
            searchType = KafkaTool.SearchType.EARLIEST;
        }
        if ((searchType == KafkaTool.SearchType.EARLIEST || searchType == KafkaTool.SearchType.LATEST) && CommonUtils.isNumeric(record.getContent())) {
            long size = Long.valueOf(record.getContent());
            if (size > 10000) {
                return Result.error(PubError.ARGS, "搜索条数不能超过1000！");
            }
        }
        kafkaTool.getEarliestOffset(topicExt.getTopicName());
        ContentType contentType = ContentType.from(topicExt.getType());
        try {
            if (contentType == ContentType.PROTO_STUFF) {
                Class<?> clazz = loadClazz(topicExt.getClassFile(), topicExt.getClassName());
                List<String> records = kafkaTool.searchTopicProtostuffContent(topicExt.getTopicName(), record.getContent(), clazz, searchType);
                return Result.success(records);
            } else {
                Class<?> clazz = null;
                if (contentType == ContentType.JSON && CommonUtils.isNotEmpty(topicExt.getClassName())) {
                    clazz = loadClazz(topicExt.getClassFile(), topicExt.getClassName());
                }
                KafkaTool.SearchType finalSearchType = searchType;
                Class<?> finalClazz = clazz;
//                DefaultThreadPool.exe(() -> {
//                    List<String> records = kafkaTool.searchTopicStringContent(topicExt.getTopicName(), record.getDescription(), finalSearchType, finalClazz);
//
//                });
                List<String> records = kafkaTool.searchTopicStringContent(topicExt.getTopicName(), record.getContent(), searchType, clazz);
                return Result.success(records);
            }

        } catch (Exception ignored) {

        }
        return new Result<>(Status.FAILED);
    }

    private Class<?> loadClazz(String path, String className) {
        String prefix = "file:" + libsDir;
        logger.debug("load class file path: {}/{}", prefix, path);
        try {
            Class<?> clazz = clazzMap.get(className);
            if (clazz == null) {
                if (CommonUtils.isEmpty(className)) {
                    clazz = Class.forName(className);
                } else {//加载过不用重新加载类对象
                    clazz = JarUtils.loadClassForJar(prefix + File.separator + path, className);
                }
                clazzMap.put(className, clazz);
            }
            return clazz;
        } catch (MalformedURLException | ClassNotFoundException e) {
            logger.error("load class to deSerialize error!", e);
        }
        return null;
    }
}
