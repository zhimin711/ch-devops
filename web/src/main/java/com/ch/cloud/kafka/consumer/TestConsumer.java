package com.ch.cloud.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * <p>
 * desc:
 * </p>
 *
 * @author zhimin.ma
 * @since 2021/9/26
 */
//@Component
@Slf4j
public class TestConsumer {

//    @KafkaListener(topics = {"${kafka.app.topic.foo}"})
    public void receive(@Payload String message, @Headers MessageHeaders headers) {
        log.info("KafkaMessageConsumer 接收到消息：" + message);
        headers.keySet().forEach(key -> log.info("{}: {}", key, headers.get(key)));
    }
}
