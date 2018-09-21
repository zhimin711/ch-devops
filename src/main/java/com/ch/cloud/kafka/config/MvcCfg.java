package com.ch.cloud.kafka.config;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
//@AutoConfigureAfter(MyBatisScannerCfg.class)
//@ComponentScan("com.ch.cloud.kafka.controller")
public class MvcCfg {

//    @Bean
    public AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor(){
        return new AutowiredAnnotationBeanPostProcessor();
    }
}
