package com.ch.cloud;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 描述：com.ch.cloud.devops
 *
 * @author zhimin.ma
 * 2017/2/22.
 * @version 1.0
 * @since 1.8
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
//@EnableKafka
@EnableFeignClients
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
