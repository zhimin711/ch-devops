package com.ch.cloud.kafka.tools;

import com.ch.utils.JSONUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import kafka.cluster.Broker;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.Iterator;
import scala.collection.Seq;

import java.util.Map;

/**
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
            zkClient = new ZkClient(zkUrl);

            zkClient.setZkSerializer(new ZkSerializer() {
                @Override
                public byte[] serialize(Object o) throws ZkMarshallingError {
                    return JSONUtils.toJson(o).getBytes(Charsets.UTF_8);
                }

                @Override
                public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                    return new String(bytes, Charsets.UTF_8);
                }
            });
            Seq<Broker> brokersInCluster = ZkUtils.getAllBrokersInCluster(zkClient);
            Iterator<Broker> iterator = brokersInCluster.iterator();
            while (iterator.hasNext()) {
                Broker broker = iterator.next();
                logger.info("broker host: {}, port: {}.", broker.host(), broker.port());
                brokers.put(broker.host(), broker.port());
            }
        } catch (Exception e) {
            logger.error("zk connect or fetch brokers error!");
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
