package com.ch.cloud.conf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/27
 */
@Configuration
@MapperScan("com.ch.cloud.devops.mapper2")
public class WebConfiguration {


    @Bean
    @Primary
    public RestTemplate restTemplate(){
        //设置超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(300000);
        factory.setConnectTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(factory);
        // 找出并修改默认的StringHttpMessageConverter
        // 关闭Accept-Charset的输出&#xff08;防止输出超长的编码列表&#xff09;
        // 设置默认编码为UTF-8
        boolean stringConverterFound = false;
        for (HttpMessageConverter<?> httpMessageConverter : restTemplate.getMessageConverters()) {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                StringHttpMessageConverter stringHttpMessageConverter = (StringHttpMessageConverter)httpMessageConverter;
                stringHttpMessageConverter.setWriteAcceptCharset(false);
                stringHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
                stringConverterFound = true;
                break;
            }
        }
        if (!stringConverterFound) {
            // 如果不存在StringHttpMessageC onverter&#xff0c;则创建一个
            StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
            stringHttpMessageConverter.setWriteAcceptCharset(false);
            restTemplate.getMessageConverters().add(stringHttpMessageConverter);
        }
        return restTemplate;
    }
    // 创建用于重试的retryTemplate
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);//配置的重试次数
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
