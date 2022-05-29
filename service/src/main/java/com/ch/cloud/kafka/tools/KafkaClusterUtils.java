package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.google.common.collect.Maps;
import kafka.cluster.Broker;
import kafka.cluster.EndPoint;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import scala.collection.Iterator;
import scala.collection.Seq;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Kafka管理工具
 *
 * @author zhimin.ma
 * @since 2018/9/19 16:36
 */
public class KafkaClusterUtils {

    private final static Logger logger = LoggerFactory.getLogger(KafkaClusterUtils.class);

    private final static ConcurrentHashMap<String, AdminClient> clients = new ConcurrentHashMap<>();

    public static final String CONSUMER_GROUP_ID = "kafka-temp";

//    private final ConcurrentHashMap<String, DelayMessageHelper> store = new ConcurrentHashMap<>();

    /*
     *查看所有主题
     *kafka-topics.sh --zookeeper localhost:2181 --list
     */
    public static Map<String, Integer> getAllBrokersInCluster(String zkUrl) {
        ZkUtils zkUtils = null;
        Map<String, Integer> brokers = Maps.newHashMap();
        try {
            zkUtils = ZkUtils.apply(zkUrl, 30000, 30000, JaasUtils.isZkSaslEnabled());

            Seq<Broker> brokersInCluster = zkUtils.getAllBrokersInCluster();
            Iterator<Broker> iterator = brokersInCluster.iterator();
            while (iterator.hasNext()) {
                Broker broker = iterator.next();
                Seq<EndPoint> eps = broker.endPoints();
                Iterator<EndPoint> ie = eps.iterator();
                while (ie.hasNext()) {
                    EndPoint endPoint = ie.next();
                    logger.info("broker host: {}, port: {}.", endPoint.host(), endPoint.port());
                    brokers.put(endPoint.host(), endPoint.port());
                }
            }
        } catch (ZkTimeoutException e) {
            ExceptionUtils._throw(PubError.CONNECT, "连接错误，请稍后重试...", e);
        } catch (Exception e) {
            logger.error("zk fetch brokers error!", e);
        } finally {
            close(zkUtils);
        }
        return brokers;
    }


    private static void close(ZkUtils zkUtils) {
        if (zkUtils != null) {
            zkUtils.close();
        }
    }

    private static AdminClient createAdminClient(String servers, String securityProtocol, String saslMechanism, String authUsername, String authPassword) {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        properties.put(AdminClientConfig.RETRIES_CONFIG, "0");
        if (StringUtils.hasText(securityProtocol)) {
            properties.put("security.protocol", securityProtocol);
        }
        if (StringUtils.hasText(saslMechanism)) {
            properties.put("sasl.mechanism", saslMechanism);
        }

        if (StringUtils.hasText(authUsername) && StringUtils.hasText(authPassword)) {
            properties.put("sasl.jaas.config", MessageFormat.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"{0}\" password=\"{1}\";", authUsername, authPassword));
        }
        return AdminClient.create(properties);
    }

    public static AdminClient getAdminClient(String id, String servers, String securityProtocol, String saslMechanism, String authUsername, String authPassword) {
        synchronized (id.intern()) {
            AdminClient adminClient = clients.get(id);
            if (adminClient == null) {
                adminClient = createAdminClient(servers, securityProtocol, saslMechanism, authUsername, authPassword);
                clients.put(id, adminClient);
            }
            return adminClient;
        }
    }

    public static AdminClient getAdminClient(KafkaCluster cluster) {
        ExceptionUtils.assertEmpty(cluster, PubError.NOT_EXISTS, "cluster config");
        synchronized (cluster.getClusterName().intern()) {
            AdminClient adminClient = clients.get(cluster.getClusterName());
            if (adminClient == null) {
                adminClient = createAdminClient(cluster.getBrokers(), cluster.getSecurityProtocol(), cluster.getSaslMechanism(), cluster.getAuthUsername(), cluster.getAuthPassword());
                clients.put(cluster.getClusterName(), adminClient);
            }
            return adminClient;
        }
    }

    public static KafkaConsumer<String, String> createConsumer(KafkaCluster config) {
        return createConsumer(config.getBrokers(), CONSUMER_GROUP_ID, "earliest",
                config.getSecurityProtocol(), config.getSaslMechanism(), config.getAuthUsername(), config.getAuthPassword());
    }

    public static KafkaConsumer<String, String> createConsumer(String servers, String groupId, String autoOffsetResetConfig, String securityProtocol, String saslMechanism, String authUsername, String authPassword) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        if (StringUtils.hasText(securityProtocol)) {
            properties.put("security.protocol", securityProtocol);
        }
        if (StringUtils.hasText(saslMechanism)) {
            properties.put("sasl.mechanism", saslMechanism);
        }

        if (StringUtils.hasText(authUsername) && StringUtils.hasText(authPassword)) {
            properties.put("sasl.jaas.config", MessageFormat.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"{0}\" password=\"{1}\";", authUsername, authPassword));
        }
        return new KafkaConsumer<>(properties, new StringDeserializer(), new StringDeserializer());
    }

    public static KafkaProducer<String, String> createProducer(String servers, String securityProtocol, String saslMechanism, String authUsername, String authPassword) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        if (StringUtils.hasText(securityProtocol)) {
            properties.put("security.protocol", securityProtocol);
        }
        if (StringUtils.hasText(saslMechanism)) {
            properties.put("sasl.mechanism", saslMechanism);
        }

        if (StringUtils.hasText(authUsername) && StringUtils.hasText(authPassword)) {
            properties.put("sasl.jaas.config", MessageFormat.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"{0}\" password=\"{1}\";", authUsername, authPassword));
        }
        return new KafkaProducer<>(properties, new StringSerializer(), new StringSerializer());
    }

    public static int countBroker(KafkaCluster cluster) throws ExecutionException, InterruptedException {
        AdminClient adminClient = getAdminClient(cluster);
        DescribeClusterResult describeClusterResult = adminClient.describeCluster();
        return describeClusterResult.nodes().get().size();
    }

    public static Set<String> fetchTopicNames(KafkaCluster cluster) throws ExecutionException, InterruptedException {
        AdminClient adminClient = getAdminClient(cluster);
        ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
        listTopicsOptions.timeoutMs(5000);
        listTopicsOptions.listInternal(false);
        ListTopicsResult topics = adminClient.listTopics(listTopicsOptions);
        return topics.names().get();
    }

    public static int countConsumerGroup(KafkaCluster cluster) throws ExecutionException, InterruptedException {
        AdminClient adminClient = getAdminClient(cluster);
        return adminClient.listConsumerGroups().all().get().size();
    }
}
