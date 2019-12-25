package com.ch.cloud.kafka.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/12/24
 */
@Configuration
public class RedisCfg {

    @Bean(destroyMethod="shutdown")
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(new ClassPathResource("config/redisson-sentinel.yml").getInputStream());
        return Redisson.create(config);
    }
}
