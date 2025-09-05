package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.pojo.TopicConfig;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.admin.TopicCommand;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.common.security.JaasUtils;
import scala.collection.JavaConversions;
import scala.collection.Map;
import scala.collection.Seq;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author zhimin.ma
 * @since 2018/9/19 16:36
 */
@Slf4j
public class ZkTopicUtils {

    /*
     * 创建主题(注：创建不成功)
     * kafka-topics.sh --zookeeper localhost:2181 --create
     * --topic kafka-action --replication-factor 2 --partitions 3
     */
    public static void createTopic(TopicConfig config) {
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply(config.getZookeeper(), 30000, 30000, JaasUtils.isZkSaslEnabled());
            // 创建一个单分区单副本名为t1的topic
            AdminUtils.createTopic(zkUtils, config.getTopicName(), config.getPartitions(), config.getReplicationFactor(), config.getProperties(), RackAwareMode.Enforced$.MODULE$);
            zkUtils.close();
        } catch (Exception e) {
            log.error("zk connect or topic create error!", e);
        } finally {
            close(zkUtils);
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
        String s = "--zookeeper 10.203.248.126:2181,10.203.248.127:2181,10.203.248.128:2181,10.203.248.129:2181,10.203.248.130:2181/kafka/eostbpcore" + " --create --topic SHIVA_TRTMS_GROUND_PLAN_ORDER_NOTIFY2" + " --partitions 3 --replication-factor 1" + " --config max.message.bytes=204800 --config flush.messages=2";
        String s1 = "--zookeeper " + config.getZookeeper() + " --create --topic " + config.getTopicName() + " --partitions " + config.getPartitions() + " --replication-factor " + config.getReplicationFactor();
        if (!configs.isEmpty()) {
            s1 += String.join(" ", configs);
        }
        String[] args = s1.split(" ");
        log.info(Arrays.toString(args));
        TopicCommand.main(args);
    }

    /**
     * 除某主题
     * kafka-topics.sh --zookeeper localhost:2181 --topic kafka-action --delete
     */
    private static void deleteTopic(TopicConfig config) {
        deleteTopic(config.getZookeeper(), config.getTopicName());
    }

    /*
     *删除某主题
     *kafka-topics.sh --zookeeper localhost:2181 --topic kafka-action --delete
     */
    public static void deleteTopic(String zkUrl, String topic) {
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply(zkUrl, 30000, 30000, JaasUtils.isZkSaslEnabled());
            // 删除topic 't1'
            AdminUtils.deleteTopic(zkUtils, topic);
            zkUtils.close();
        } catch (Exception e) {
            log.error("delete error! => {}", topic, e);
        } finally {
            close(zkUtils);
        }
    }

    /**
     * 修改主题配置     kafka-config --zookeeper localhost:2181 --entity-type topics --entity-name kafka-action
     * --alter --add-config max.message.bytes=202480 --alter --delete-config flush.messages
     */
    private static void updateTopic(TopicConfig config) {
        ZkUtils zkUtils = ZkUtils.apply(config.getZookeeper(), 30000, 30000, JaasUtils.isZkSaslEnabled());
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), config.getTopicName());
        // 增加topic级别属性
//        props.put("min.cleanable.dirty.ratio", "0.3");
        // 删除topic级别属性
//        props.remove("max.message.bytes");
        // 修改topic 'test'的属性
        props.putAll(config.getProperties());
        AdminUtils.changeTopicConfig(zkUtils, config.getTopicName(), props);
        zkUtils.close();
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
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply(zkUrl, 30000, 30000, JaasUtils.isZkSaslEnabled());
            List<String> topics = JavaConversions.seqAsJavaList(zkUtils.getAllTopics());
            return topics.stream().filter(e -> CommonUtils.isEmpty(topicName) || e.contains(topicName)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("zk connect or fetch topics error!" + topicName, e);
        } finally {
            close(zkUtils);
        }
        return Lists.newArrayList();
    }


    /**
     * 得到所有topic的配置信息
     * kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --describe
     */
    public static void listTopicAllConfig(String zkUrl) {
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply(zkUrl, 30000, 30000, JaasUtils.isZkSaslEnabled());
            java.util.Map<String, Properties> configs = JavaConversions.mapAsJavaMap(AdminUtils.fetchAllTopicConfigs(zkUtils));
            // 获取特定topic的元数据
//            MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk("topic-19",zkUtils);
            // 获取特定topic的配置信息
//            Properties properties = AdminUtils.fetchEntityConfig(zkUtils,"topics","kafka-test");
//            for (Map.Entry<String, Properties> entry : configs.entrySet()) {
//                System.out.println("key=" + entry.getKey() + " ;value= " + entry.getValue());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(zkUtils);
        }
    }

    private static void close(ZkClient zkClient) {
        if (zkClient != null) {
            zkClient.close();
        }
    }

    private static void close(ZkUtils zkUtils) {
        if (zkUtils != null) {
            zkUtils.close();
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
     /*       try {
                boolean exists = AdminUtils.topicExists(zkClient, e);
                if (!exists) return;
                TopicInfo info = getTopicInfo(zkClient, e);
                topics.add(info);
            } catch (Exception ex) {
                log.error("get topic info error!", ex);
            }*/
        });
        close(zkClient);
        return topics;
    }

    public static List<TopicInfo> getTopics(String zkUrl) {
        ZkClient zkClient = null;
        ZkConnection zkConnection = null;
        List<TopicInfo> topics = Lists.newArrayList();
        try {
            zkClient = new ZkClient(zkUrl, 360000);
            zkConnection = new ZkConnection(zkUrl, 360000);
            zkClient.setZkSerializer(KafkaSerializeUtils.jsonZk());
            ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);
            Seq<String> topicSeq = zkUtils.getAllTopics();
            Map<String, Properties> config = AdminUtils.fetchAllTopicConfigs(zkUtils);

            java.util.Map<String, Properties> configs = JavaConversions.mapAsJavaMap(config);
            System.out.println(configs);
/*
            Iterator<TopicMetadata> metadataIterator = topicMetadataSet.iterator();
            while (metadataIterator.hasNext()) {
                kafka.javaapi.TopicMetadata meta = new kafka.javaapi.TopicMetadata(metadataIterator.next());
                TopicInfo info = convertTopicMeta(meta, meta.topic());
                topics.add(info);
            }*/
        } catch (Exception e) {
            log.error("zk connect or fetch topics error!");
        } finally {
            close(zkClient);
            close(zkConnection);
        }
        return topics;
    }

    private static void close(ZkConnection zkConnection) {
        if (zkConnection != null) {
            try {
                zkConnection.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
            /*Seq<String> topicSeq = ZkUtils.getAllTopics(zkClient);
            Iterator<String> iter1 = topicSeq.iterator();
            while (iter1.hasNext()) {
                String name = iter1.next();
                TopicInfo info = getTopicInfo(zkClient, name);
                topics.add(info);
            }*/
        } catch (Exception e) {
            log.error("zk connect or fetch topics error!");
        } finally {
            close(zkClient);
        }
        return topics;
    }

  /*  private static TopicInfo getTopicInfo(ZkClient zkClient, String name) {
        TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(name, zkClient);
        kafka.javaapi.TopicMetadata meta = new kafka.javaapi.TopicMetadata(topicMetadata);
        return convertTopicMeta(meta, name);
    }*/
}
