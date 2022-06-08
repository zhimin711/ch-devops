package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.dto.KafkaMessageDTO;
import com.ch.cloud.kafka.enums.SearchType;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.vo.KafkaContentSearchVO;
import com.ch.cloud.kafka.vo.KafkaMessageVO;
import com.ch.utils.CommonUtils;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        RecordMetadata recordMetadata;
        try (KafkaProducer<String, String> kafkaProducer =
            KafkaClusterUtils.createProducer(cluster.getBrokers(), cluster.getSecurityProtocol(),
                cluster.getSaslMechanism(), cluster.getAuthUsername(), cluster.getAuthPassword())) {
            ProducerRecord<String, String> producerRecord;
            if (CommonUtils.isEmpty(messageVO.getPartition())) {
                producerRecord = new ProducerRecord<>(messageVO.getTopic(), messageVO.getKey(), messageVO.getValue());
            } else {
                producerRecord = new ProducerRecord<>(messageVO.getTopic(), messageVO.getPartition(),
                    messageVO.getKey(), messageVO.getValue());
            }

            recordMetadata = kafkaProducer.send(producerRecord).get();
        }
        return recordMetadata.offset();
    }

    @SneakyThrows
    public long send(KafkaMessageVO messageVO, byte[] data) {
        KafkaCluster cluster = kafkaClusterService.find(messageVO.getClusterId());
        RecordMetadata recordMetadata;
        try (KafkaProducer<String, byte[]> kafkaProducer =
            KafkaClusterUtils.createProducerByte(cluster.getBrokers(), cluster.getSecurityProtocol(),
                cluster.getSaslMechanism(), cluster.getAuthUsername(), cluster.getAuthPassword())) {
            ProducerRecord<String, byte[]> producerRecord;
            if (CommonUtils.isEmpty(messageVO.getPartition())) {
                producerRecord = new ProducerRecord<>(messageVO.getTopic(), messageVO.getKey(), data);
            } else {
                producerRecord =
                    new ProducerRecord<>(messageVO.getTopic(), messageVO.getPartition(), messageVO.getKey(), data);
            }

            recordMetadata = kafkaProducer.send(producerRecord).get();
        }
        return recordMetadata.offset();
    }

    public List<KafkaMessageDTO> search(KafkaContentSearchVO searchVO, Integer tPartition, Long startOffset,
        Long endOffset) {
        Long clusterId = searchVO.getClusterId();
        String topicName = searchVO.getTopic();
        String keyFilter = searchVO.getKey();
        String valueFilter = searchVO.getContent();
        int count = searchVO.getLimit();
        KafkaCluster cluster = kafkaClusterService.find(clusterId);

        tPartition = CommonUtils.or(tPartition, searchVO.getPartition());

        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClusterUtils.createConsumer(cluster)) {

            TopicPartition topicPartition = new TopicPartition(topicName, tPartition);
            List<TopicPartition> topicPartitions = Collections.singletonList(topicPartition);
            kafkaConsumer.assign(topicPartitions);

            Long beginningOffset = kafkaConsumer.beginningOffsets(topicPartitions).get(topicPartition);
            Long endOffset2 = kafkaConsumer.endOffsets(topicPartitions).get(topicPartition);
            if (startOffset == null) {
                if (searchVO.getType() == SearchType.LATEST) {
                    startOffset = searchVO.getOffset() - searchVO.getSize();
                } else {
                    startOffset = Math.min(searchVO.getOffset(), endOffset2);
                }
            }
            if (endOffset == null) {
                if (searchVO.getType() == SearchType.LATEST) {
                    endOffset = searchVO.getOffset();
                } else {
                    endOffset = Math.min(searchVO.getOffset() + searchVO.getSize(), endOffset2);
                }
            }

            if (startOffset < beginningOffset) {
                startOffset = beginningOffset;
            }
            kafkaConsumer.seek(topicPartition, startOffset);

            long currentOffset = startOffset - 1;

            List<ConsumerRecord<String, String>> records = new ArrayList<>(count);

            int emptyPoll = 0;
            while (records.size() < count && currentOffset < endOffset) {
                List<ConsumerRecord<String, String>> polled =
                    kafkaConsumer.poll(Duration.ofMillis(200)).records(topicPartition);

                if (!CollectionUtils.isEmpty(polled)) {

                    for (ConsumerRecord<String, String> consumerRecord : polled) {
                        if (StringUtils.hasText(keyFilter)) {
                            String key = consumerRecord.key();
                            if (StringUtils.hasText(key) && key.toLowerCase().contains(keyFilter.toLowerCase())) {
                                records.add(consumerRecord);
                            }
                            continue;
                        }

                        if (StringUtils.hasText(valueFilter)) {
                            String value = consumerRecord.value();
                            if (StringUtils.hasText(value) && value.toLowerCase().contains(valueFilter.toLowerCase())) {
                                records.add(consumerRecord);
                            }
                            continue;
                        }
                        records.add(consumerRecord);
                    }
                    currentOffset = polled.get(polled.size() - 1).offset();
                    emptyPoll = 0;
                } else if (++emptyPoll == 3) {
                    break;
                }
            }
            if (searchVO.getType() == SearchType.LATEST) {
                searchVO.setOffset(startOffset);
            } else {
                searchVO.setOffset(currentOffset);
            }
            return records.stream().map(record -> {
                int partition = record.partition();
                long timestamp = record.timestamp();
                String key = record.key();
                String value = record.value();
                long offset = record.offset();

                KafkaMessageDTO consumerMessage = new KafkaMessageDTO();
                consumerMessage.setTopic(topicName);
                consumerMessage.setOffset(offset);
                consumerMessage.setPartition(partition);
                consumerMessage.setTimestamp(timestamp);
                consumerMessage.setKey(key);
                consumerMessage.setValue(value);

                return consumerMessage;
            }).collect(Collectors.toList());
        }
    }

    public List<KafkaMessageDTO> search(KafkaContentSearchVO record, TopicInfo.Partition partition, Class<?> clazz) {
        long start = 0;
        long end = 0;
        switch (record.getType()) {
            case ALL:
            case EARLIEST:
                start = partition.getBeginningOffset();
                end = Math.min(partition.getBeginningOffset() + record.getSize(), partition.getEndOffset());
                break;
            case LATEST:
                start = Math.min(partition.getEndOffset() - record.getSize(), partition.getBeginningOffset());
                end = partition.getEndOffset();
        }

        return search(record, partition.getPartition(), start, end);
    }

    public List<KafkaMessageDTO> more(KafkaContentSearchVO searchVO, Class<?> clazz) {
        List<KafkaMessageDTO> list = search(searchVO, null, null, null);
        return list;
    }
}
