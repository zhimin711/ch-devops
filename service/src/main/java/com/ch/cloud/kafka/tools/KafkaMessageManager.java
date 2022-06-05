package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.vo.KafkaMessageVO;
import com.ch.utils.CommonUtils;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/6/5 20:53
 */
@Component
public class KafkaMessageManager extends AbsKafkaManager {

    @SneakyThrows
    public long send(KafkaMessageVO messageVO) {
        KafkaCluster cluster = kafkaClusterService.find(messageVO.getClusterId());
        KafkaProducer<String, String> kafkaProducer = KafkaClusterUtils.createProducer(cluster.getBrokers(), cluster.getSecurityProtocol(), cluster.getSaslMechanism(), cluster.getAuthUsername(), cluster.getAuthPassword());
        ProducerRecord<String, String> producerRecord;
        if (CommonUtils.isEmpty(messageVO.getPartition())) {
            producerRecord = new ProducerRecord<>(messageVO.getTopic(), messageVO.getKey(), messageVO.getValue());
        } else {
            producerRecord = new ProducerRecord<>(messageVO.getTopic(), messageVO.getPartition(), messageVO.getKey(), messageVO.getValue());
        }

        RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
        return recordMetadata.offset();
    }

    @SneakyThrows
    public long send(KafkaMessageVO messageVO, byte[] data) {
        KafkaCluster cluster = kafkaClusterService.find(messageVO.getClusterId());
        KafkaProducer<String, byte[]> kafkaProducer = KafkaClusterUtils.createProducerByte(cluster.getBrokers(), cluster.getSecurityProtocol(), cluster.getSaslMechanism(), cluster.getAuthUsername(), cluster.getAuthPassword());
        ProducerRecord<String, byte[]> producerRecord;
        if (CommonUtils.isEmpty(messageVO.getPartition())) {
            producerRecord = new ProducerRecord<>(messageVO.getTopic(), messageVO.getKey(), data);
        } else {
            producerRecord = new ProducerRecord<>(messageVO.getTopic(), messageVO.getPartition(), messageVO.getKey(), data);
        }

        RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
        return recordMetadata.offset();
    }
}
