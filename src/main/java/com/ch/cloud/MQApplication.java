package com.ch.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 描述：com.ch.cloud.mq
 *
 * @author zhimin.ma
 * 2017/2/22.
 * @version 1.0
 * @since 1.8
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MQApplication {
    public static void main(String[] args) {
        SpringApplication.run(MQApplication.class, args);
    }
}
