package com.ch.cloud.nacos.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2024/6/8
 */
@ConfigurationProperties(prefix = "nacos")
@Data
public class NacosProperties {
    
    private String loginApi;
    
}
