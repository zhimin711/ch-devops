package com.ch.cloud.kafka.api.impl;

import com.ch.cloud.kafka.api.SearchService;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.pojo.ContentQuery;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.TopicExtService;
import com.ch.cloud.kafka.tools.KafkaTool;
import com.ch.err.ErrorCode;
import com.ch.result.BaseResult;
import com.ch.type.Status;
import com.ch.utils.JarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.List;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Value("${share.path.libs}")
    private String libsDir;
    private final String PATH_PROTOCOL = "file:";

    @Autowired
    private ClusterConfigService clusterConfigService;
    @Autowired
    private TopicExtService topicExtService;

    public BaseResult<String> searchContent(ContentQuery record) {
        BtClusterConfig config = clusterConfigService.findByClusterName(record.getCluster());
        BtTopicExt topicExt = topicExtService.findByClusterAndTopic(record.getCluster(), record.getTopic());

        KafkaTool kafkaTool = new KafkaTool(config.getBrokers());

        if (record.getType() == ContentQuery.Type.PROTO_STUFF) {
            try {
                Class<?> clazz = JarUtils.loadClassForJar(PATH_PROTOCOL + topicExt.getClassFile(), topicExt.getClassName());
                List<String> records = kafkaTool.searchTopicProtostuffContent(record.getTopic(), record.getContent(), clazz);
                return new BaseResult<>(records);
            } catch (MalformedURLException | ClassNotFoundException e) {
                e.printStackTrace();
                return new BaseResult<>(ErrorCode.ARGS, "序列化类：" + topicExt.getClassName() + "不存在！");
            }
        } else {
            try {

                List<String> records = kafkaTool.searchTopicStringContent(topicExt.getTopicName(), record.getContent());
                return new BaseResult<>(records);
            } catch (Exception ignored) {

            }
        }

        return new BaseResult<>(Status.SUCCESS);
    }
}
