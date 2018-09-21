package com.ch.cloud.kafka.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import java.util.Properties;

/**
 * 描述：MyBatis tk.mapper 通用配置
 *
 * @author 80002023
 *         2017/2/22.
 * @version 1.0
 * @since 1.8
 */
@Configuration
@AutoConfigureAfter(MybatisCfg.class)
public class MyBatisScannerCfg {

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");

        StringBuffer packages = new StringBuffer();
        packages.append("com.ch.cloud.kafka.**.mapper");

        mapperScannerConfigurer.setBasePackage(packages.toString());
        Properties properties = new Properties();
        properties.setProperty("mappers", "tk.mybatis.mapper.common.Mapper");
        mapperScannerConfigurer.setProperties(properties);
        return mapperScannerConfigurer;
    }

}
