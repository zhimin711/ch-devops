package com.ch.cloud.nacos.autoconfigurate;

import com.ch.cloud.nacos.conf.NacosProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2024/6/8
 */
@Configuration
@EnableConfigurationProperties(NacosProperties.class)
public class NacosAutoConfiguration {

}
