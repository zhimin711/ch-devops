package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.dto.KafkaTopicConfigDTO;
import com.ch.cloud.kafka.model.KafkaTopic;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/6/5 20:36
 */
@Component
public class KafkaTopicManager extends AbsKafkaManager {


    public void createTopic(KafkaTopic topic) throws ExecutionException, InterruptedException {
        AdminClient adminClient = getAdminClient(topic.getClusterId());
        NewTopic newTopic = new NewTopic(topic.getTopicName(), topic.getPartitionSize(), topic.getReplicaSize().shortValue());
        CreateTopicsResult topicsResult = adminClient.createTopics(Collections.singleton(newTopic));
        KafkaFuture<Void> kafkaFuture = topicsResult.all();
        kafkaFuture.get();
    }

    public void deleteTopic(Long clusterId, List<String> topics) throws ExecutionException, InterruptedException {
        AdminClient adminClient = getAdminClient(clusterId);
        DeleteTopicsResult topicsResult = adminClient.deleteTopics(topics);
        KafkaFuture<Void> all = topicsResult.all();
        all.get();
    }

    @SneakyThrows
    public List<KafkaTopicConfigDTO> getConfigs(Long clusterId, String topic) {
        AdminClient adminClient = getAdminClient(clusterId);

        ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        Config config = adminClient.describeConfigs(Collections.singletonList(configResource)).all().get().get(configResource);

        return config.entries()
                .stream()
                .map(entry -> {
                    KafkaTopicConfigDTO topicConfig = new KafkaTopicConfigDTO();
                    topicConfig.setName(entry.name());
                    topicConfig.setValue(entry.value());
                    topicConfig.set_default(entry.isDefault());
                    topicConfig.setReadonly(entry.isReadOnly());
                    topicConfig.setSensitive(entry.isSensitive());
                    return topicConfig;
                })
                .collect(Collectors.toList());
    }

    public void setConfigs(Long clusterId, String topic, Map<String, String> configs) throws ExecutionException, InterruptedException {
        AdminClient adminClient = getAdminClient(clusterId);
        ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);

        List<AlterConfigOp> alterConfigOps = configs.entrySet()
                .stream()
                .map(e -> {
                    String key = e.getKey();
                    String value = e.getValue();
                    ConfigEntry configEntry = new ConfigEntry(key, value);
                    return new AlterConfigOp(configEntry, AlterConfigOp.OpType.SET);
                })
                .collect(Collectors.toList());

        Map<ConfigResource, Collection<AlterConfigOp>> data = new HashMap<>();
        data.put(configResource, alterConfigOps);

        adminClient.incrementalAlterConfigs(data).all().get();
    }
}
