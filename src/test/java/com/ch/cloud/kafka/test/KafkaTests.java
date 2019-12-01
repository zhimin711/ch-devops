package com.ch.cloud.kafka.test;

import com.ch.cloud.kafka.KafkaApplication;
import com.ch.utils.JSONUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

/**
 * @author 01370603
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaApplication.class)
public class KafkaTests {

    private final Logger logger = LoggerFactory.getLogger(KafkaTests.class);
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    Object o;

    @Test
    //发送消息方法
    public void send() {
        Message message = new Message();
        message.setId(System.currentTimeMillis());
        message.setMsg(UUID.randomUUID().toString());
        message.setSendTime(new Date());
        logger.info("+++++++++++++++++++++  message = {}", JSONUtils.toJson(message));
        kafkaTemplate.send("zhisheng", JSONUtils.toJson(message));
    }


    public class Message {
        private Long id;    //id

        private String msg; //消息

        private Date sendTime;  //时间戳

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Date getSendTime() {
            return sendTime;
        }

        public void setSendTime(Date sendTime) {
            this.sendTime = sendTime;
        }
    }


}
