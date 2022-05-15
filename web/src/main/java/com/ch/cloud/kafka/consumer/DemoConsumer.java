package com.ch.cloud.kafka.consumer;

import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhimin.ma
 * @since 2018/9/21 11:47
 */
public class DemoConsumer {
    private Logger logger = LoggerFactory.getLogger(DemoConsumer.class);

    private String topic;

    private ConsumerConnector consumer;

    public DemoConsumer(String servers, String groupId, String topic) {
        Properties props = new Properties();

        props.put("zookeeper.connect", servers);
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.20.211:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        //  smallest,earliest,largest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "smallest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        kafka.consumer.ConsumerConfig consumerConfig = new kafka.consumer.ConsumerConfig(props);
        //构建consumer connection 对象
        this.consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
        this.topic = topic;
    }


    public void nextTuple() {
        try {
            //消费数据
            Map<String, Integer> topicCountMap = new HashMap<>();
            int localConsumerCount = 2;
            topicCountMap.put(topic, localConsumerCount);
            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);

            List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

            streams.forEach(stream -> {
                for (MessageAndMetadata<byte[], byte[]> msg : stream) {

                    String str = new String(msg.message());
                    logger.info("consumer ======================> {} str: {}", msg.offset(), str);
                }
            });
        } catch (Exception e) {
            logger.error("consumer Error!", e);
            throw new RuntimeException("Error consumer tuple", e);
        }
    }

    public void destroy(){
        consumer.shutdown();
    }

}
