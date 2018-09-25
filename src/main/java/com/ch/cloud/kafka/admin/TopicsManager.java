package com.ch.cloud.kafka.admin;

import com.google.common.collect.Lists;
import kafka.admin.AdminUtils;
import kafka.admin.TopicCommand;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.Iterator;
import scala.collection.Seq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @author 01370603
 * @date 2018/9/19 16:36
 */
public class TopicsManager {

    private final static Logger logger = LoggerFactory.getLogger(TopicsManager.class);

    /*
    创建主题
    kafka-topics.sh --zookeeper localhost:2181 --create
    --topic kafka-action --replication-factor 2 --partitions 3
     */
    public static void createTopic(TopicConfig config) {
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(config.getZookeeper());
            System.out.println(config);
            if (!AdminUtils.topicExists(zkClient, config.getTopicName())) {
                AdminUtils.createTopic(zkClient, config.getTopicName(), config.getPartitions(),
                        config.getReplicationFactor(), config.getProperties());
                logger.info("messages:successful create!");
            } else {
                logger.error(config.getTopicName() + " is exits!");
            }

        } catch (Exception e) {
            logger.error("zk connect or topic create error!");
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
     *               执行：TopicsManager.createTopic(s);
     */
    public static void createTopicByCommand(String config) {
        String[] args = config.split(" ");
        System.out.println(Arrays.toString(args));
        TopicCommand.main(args);
    }

    /*
     *查看所有主题
     *kafka-topics.sh --zookeeper localhost:2181 --list
     */
    public static List<String> getAllTopics(String zkUrl) {
        ZkClient zkClient = null;
        List<String> topics = Lists.newArrayList();
        try {
            zkClient = new ZkClient(zkUrl);
            Seq<String> topicSeq = ZkUtils.getAllTopics(zkClient);
            Iterator<String> iterator = topicSeq.iterator();
            while (iterator.hasNext()) {
                String topic = iterator.next();
                logger.info("topic: {}", topic);
                topics.add(topic);
            }
        } catch (Exception e) {
            logger.error("zk connect or fetch topics error!");
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
            //先取得原始的参数，然后添加新的参数同时去除需要去除的参数
            Properties oldProperties = AdminUtils.fetchTopicConfig(zkClient, topicName);
            properties.putAll(new HashMap<>(oldProperties));
            properties.remove("max.message.bytes");
            AdminUtils.changeTopicConfig(zkClient, topicName, properties);
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
            e.printStackTrace();
        } finally {
            close(zkClient);
        }
    }

    /**
     * 得到所有topic的配置信息
     * kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --describe
     */
    public static void listTopicAllConfig(String zkUrl) {
        try {
            ZkClient zkClient = new ZkClient(zkUrl);
            Seq<String> topics = ZkUtils.getAllTopics(zkClient);
            Iterator<String> iterator = topics.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }
            zkClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void close(ZkClient zkClient) {
        if (zkClient != null) {
            zkClient.close();
        }
    }
}
