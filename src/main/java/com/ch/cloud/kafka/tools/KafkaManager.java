package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.e.PubError;
import com.ch.utils.ExceptionUtils;
import com.ch.utils.JSONUtils;
import com.google.common.collect.Maps;
import kafka.cluster.Broker;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.Iterator;
import scala.collection.Seq;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Kafka管理工具
 *
 * @author 01370603
 * @date 2018/9/19 16:36
 */
public class KafkaManager {

    private final static Logger logger = LoggerFactory.getLogger(KafkaManager.class);

    /*
     *查看所有主题
     *kafka-topics.sh --zookeeper localhost:2181 --list
     */
    public static Map<String, Integer> getAllBrokersInCluster(String zkUrl) {
        ZkClient zkClient = null;
        Map<String, Integer> brokers = Maps.newHashMap();
        try {
            zkClient = new ZkClient(zkUrl, 30000);

            zkClient.setZkSerializer(KafkaSerializeUtils.jsonZk());

            Seq<Broker> brokersInCluster = ZkUtils.getAllBrokersInCluster(zkClient);
            Iterator<Broker> iterator = brokersInCluster.iterator();
            while (iterator.hasNext()) {
                Broker broker = iterator.next();
                logger.info("broker host: {}, port: {}.", broker.host(), broker.port());
                brokers.put(broker.host(), broker.port());
            }
        } catch (ZkTimeoutException e) {
            ExceptionUtils._throw(PubError.CONNECT, "连接错误，请稍后重试...", e);
        } catch (Exception e) {
            logger.error("zk fetch brokers error!", e);
        } finally {
            close(zkClient);
        }
        return brokers;
    }


    private static void close(ZkClient zkClient) {
        if (zkClient != null) {
            zkClient.close();
        }
    }
}
