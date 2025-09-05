package com.ch.cloud.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhimin.ma
 * @since 2021/4/10 0:56
 */
@Data
public class KafkaContentSearchDTO {

    /**
     * 集群名称
     */
    @Schema(description = "集群ID")
    private Long clusterId;
    /**
     * 集群主题
     */
    @Schema(description = "集群主题")
    private String topic;

    /**
     * 分区搜索最后位置
     */
    @Schema(description = "分区搜索最后位置")
    private Map<Integer, Long> partitionOffset = new HashMap<>();
    /**
     * 分区搜索到的消息
     */
    @Schema(description = "分区消息")
    private Map<Integer, List<KafkaMessageDTO>> partitionMessages = new HashMap<>();

    public void putOffset(int partition, long offset) {
        partitionOffset.put(partition, offset);
    }

    public void putMessages(int partition, List<KafkaMessageDTO> messages) {
        partitionMessages.put(partition, messages);
    }
}