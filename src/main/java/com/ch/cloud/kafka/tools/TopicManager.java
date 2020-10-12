package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.pojo.TopicConfig;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import kafka.admin.AdminUtils;
import kafka.admin.TopicCommand;
import kafka.api.TopicMetadata;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import scala.Function1;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.Map;
import scala.collection.Seq;
import scala.collection.Set;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @author zhimin.ma
 * @date 2018/9/19 16:36
 */
@Slf4j
public class TopicManager {

    /*
     * 创建主题(注：创建不成功)
     * kafka-topics.sh --zookeeper localhost:2181 --create
     * --topic kafka-action --replication-factor 2 --partitions 3
     */
    public static void createTopic(TopicConfig config) {
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(config.getZookeeper());
            if (!AdminUtils.topicExists(zkClient, config.getTopicName())) {
                AdminUtils.createTopic(zkClient, config.getTopicName(), config.getPartitions(),
                        config.getReplicationFactor(), config.getProperties());

                log.info("{}:successful create!", config.getTopicName());
            } else {
                log.error(config.getTopicName() + " is exits!");
            }

        } catch (Exception e) {
            log.error("zk connect or topic create error!", e);
        } finally {
            close(zkClient);
        }
    }

    /**
     * 创建主题（采用TopicCommand的方式）
     *
     * @param config String s = "--zookeeper localhost:2181 --create --topic kafka-action " +
     *               "  --partitions 3 --replication-factor 1" +
     *               "  --if-not-exists --config max.message.bytes=204800 --config flush.messages=2";
     *               执行：TopicManager.createTopic(s);
     */
    public static void createTopicByCommand(TopicConfig config) {
        List<String> configs = Lists.newArrayList();
        if (!config.getProperties().isEmpty()) {
            config.getProperties().forEach((k, v) -> configs.add(" --config " + k + "=" + v));
        }
        String s = "--zookeeper 10.203.248.126:2181,10.203.248.127:2181,10.203.248.128:2181,10.203.248.129:2181,10.203.248.130:2181/kafka/eostbpcore" +
                " --create --topic SHIVA_TRTMS_GROUND_PLAN_ORDER_NOTIFY2" +
                " --partitions 3 --replication-factor 1" +
                " --config max.message.bytes=204800 --config flush.messages=2";
        String s1 = "--zookeeper " + config.getZookeeper() +
                " --create --topic " + config.getTopicName() +
                " --partitions " + config.getPartitions() + " --replication-factor " + config.getReplicationFactor();
        if (!configs.isEmpty()) {
            s1 += String.join(" ", configs);
        }
        String[] args = s1.split(" ");
        log.info(Arrays.toString(args));
        TopicCommand.main(args);
    }

    /*
     *查看所有主题
     *kafka-topics.sh --zookeeper localhost:2181 --list
     */
    public static List<String> getAllTopics(String zkUrl) {
        return getTopicsByName(zkUrl, null);
    }

    /*
     *查看所有主题
     *kafka-topics.sh --zookeeper localhost:2181 --list
     */
    public static List<String> getTopicsByName(String zkUrl, String topicName) {
        ZkClient zkClient = null;
        List<String> topics = Lists.newArrayList();
        try {
            zkClient = new ZkClient(zkUrl);
            Seq<String> topicSeq = ZkUtils.getAllTopics(zkClient);
            Iterator<String> iterator = topicSeq.iterator();
            while (iterator.hasNext()) {
                String topic = iterator.next();
                log.debug("topic: {}", topic);
                if (CommonUtils.isNotEmpty(topicName)) {
                    if (topic.contains(topicName)) topics.add(topic);
                } else {
                    topics.add(topic);
                }
            }
        } catch (Exception e) {
            log.error("zk connect or fetch topics error!" + topics, e);
        } finally {
            close(zkClient);
        }
        return topics;
    }

    /**
     * 修改主题配置
     * kafka-config --zookeeper localhost:2181 --entity-type topics --entity-name kafka-action
     * --alter --add-config max.message.bytes=202480 --alter --delete-config flush.messages
     */
    public static void alterTopicConfig(String zkUrl, String topicName, Properties properties) {
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(zkUrl);
            zkClient.setZkSerializer(KafkaSerializeUtils.jsonZk());
            //先取得原始的参数，然后添加新的参数同时去除需要去除的参数
            Properties oldProperties = AdminUtils.fetchTopicConfig(zkClient, topicName);
            if (!oldProperties.isEmpty()) {
                properties.putAll(new HashMap<>(oldProperties));
            }
            // 增加topic级别属性
            properties.put("min.cleanable.dirty.ratio", "0.3");
            // 删除topic级别属性
            properties.remove("max.message.bytes");
//            AdminUtils.changeTopicConfig(zkClient, topicName, properties);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(zkClient);
        }
    }

    /*
     *删除某主题
     *kafka-topics.sh --zookeeper localhost:2181 --topic kafka-action --delete
     */
    public static void deleteTopic(String zkUrl, String topic) {
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(zkUrl);
            AdminUtils.deleteTopic(zkClient, topic);
        } catch (Exception e) {
            log.error("delete error! => " + topic, e);
        } finally {
            close(zkClient);
        }
    }

    /**
     * 得到所有topic的配置信息
     * kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --describe
     */
    public static void listTopicAllConfig(String zkUrl) {
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(zkUrl);

            Map<String, Properties> configsMap = AdminUtils.fetchAllTopicConfigs(zkClient);
            Iterator<Tuple2<String, Properties>> iterator = configsMap.iterator();
            while (iterator.hasNext()) {
                Tuple2<String, Properties> tuple2 = iterator.next();
                System.out.println(tuple2._1 + tuple2._2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(zkClient);
        }
    }

    private static void close(ZkClient zkClient) {
        if (zkClient != null) {
            zkClient.close();
        }
    }

    public static TopicInfo getInfo(String zkUrl, String topic) {
        List<TopicInfo> infos = getInfos(zkUrl, Lists.newArrayList(topic));
        return infos.isEmpty() ? null : infos.get(0);
    }

    public static List<TopicInfo> getInfos(String zkUrl, List<String> topicNames) {
        ZkClient zkClient = new ZkClient(zkUrl, 60000);
        zkClient.setZkSerializer(KafkaSerializeUtils.jsonZk());

        List<TopicInfo> topics = Lists.newArrayList();
        topicNames.forEach(e -> {
            try {
                boolean exists = AdminUtils.topicExists(zkClient, e);
                if (!exists) return;
                TopicInfo info = getTopicInfo(zkClient, e);
                topics.add(info);
            } catch (Exception ex) {
                log.error("get topic info error!", ex);
            }
        });
        close(zkClient);
        return topics;
    }

    public static List<TopicInfo> getTopics(String zkUrl) {
        ZkClient zkClient = null;
        List<TopicInfo> topics = Lists.newArrayList();
        try {
            zkClient = new ZkClient(zkUrl, 360000);
            zkClient.setZkSerializer(KafkaSerializeUtils.jsonZk());
            Seq<String> topicSeq = ZkUtils.getAllTopics(zkClient);
            Set<TopicMetadata> topicMetadataSet = AdminUtils.fetchTopicMetadataFromZk(topicSeq.toSet(), zkClient);

            Iterator<TopicMetadata> metadataIterator = topicMetadataSet.iterator();
            while (metadataIterator.hasNext()) {
                kafka.javaapi.TopicMetadata meta = new kafka.javaapi.TopicMetadata(metadataIterator.next());
                TopicInfo info = convertTopicMeta(meta, meta.topic());
                topics.add(info);
            }
        } catch (Exception e) {
            log.error("zk connect or fetch topics error!");
        } finally {
            close(zkClient);
        }
        return topics;
    }

    private static TopicInfo convertTopicMeta(kafka.javaapi.TopicMetadata meta, String topic) {
        int partSize = meta.partitionsMetadata().size();
        TopicInfo info = new TopicInfo();
        info.setName(topic);
        info.setPartitionSize(partSize);
        if (partSize > 0) {
            int replicaSize = meta.partitionsMetadata().get(0).replicas().size();
            info.setReplicaSize(replicaSize);
        }
        return info;
    }

    public static List<TopicInfo> getTopics2(String zkUrl) {
        ZkClient zkClient = null;
        List<TopicInfo> topics = Lists.newArrayList();
        try {
            zkClient = new ZkClient(zkUrl, 360000);
            zkClient.setZkSerializer(KafkaSerializeUtils.jsonZk());
            Seq<String> topicSeq = ZkUtils.getAllTopics(zkClient);
            Iterator<String> iter1 = topicSeq.iterator();
            while (iter1.hasNext()) {
                String name = iter1.next();
                TopicInfo info = getTopicInfo(zkClient, name);
                topics.add(info);
            }
        } catch (Exception e) {
            log.error("zk connect or fetch topics error!");
        } finally {
            close(zkClient);
        }
        return topics;
    }

    private static TopicInfo getTopicInfo(ZkClient zkClient, String name) {
        TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(name, zkClient);
        kafka.javaapi.TopicMetadata meta = new kafka.javaapi.TopicMetadata(topicMetadata);
        return convertTopicMeta(meta, name);
    }
}
