package com.ch.cloud.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 描述：com.ch.cloud.kafka
 *
 * @author 80002023
 * 2017/2/22.
 * @version 1.0
 * @since 1.8
 */
@SpringBootApplication
//@Configuration
//@EnableAutoConfiguration
//@EnableDiscoveryClient
public class KafkaApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args);
    }
}
