package com.ch.cloud.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author: Wang Chen Chen
 * @Date: 2018/10/26 10:42
 * @describe： swagger2 RESTful接口文档配置
 *             文档URL: http://localhost:7004/swagger2.html
 * @version: 1.0
 */

@EnableSwagger2
@Configuration
// 启用swagger2功能注解
public class Swagger2Config {

    @Bean
    public Docket buildDocket(){
        //api文档实例
        //文档类型：DocumentationType.SWAGGER_2
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())    //.apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ch.cloud.kafka.controller"))//要注释的接口名
                .paths(PathSelectors.any())
                .build();
    }

    /**
     *@describe 接口的相关信息
     *@date 2018/11/7
     *@author  Wang Chen Chen
     */
    private ApiInfo apiInfo2() {
        return new ApiInfoBuilder()
                .title("朝华后台 Swagger2 构建RESTful接口 ")
                .description("接口描述")
                .termsOfServiceUrl("termsOfServiceUrl")
                .version("1.0")
                .license("http://springfox.github.io/springfox/docs/current/")
                .licenseUrl("http://springfox.github.io/springfox/docs/current/")
                .build();
    }

    //构建 api文档的详细信息函数,注意这里的注解引用的是哪个
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("朝华 Kafka RESTFUL API")
                //创建人
                .contact(new Contact("CHAO YUN", "http://www.chaoyun.com", ""))
                //版本号
                .version("1.0.0")
                //描述
                .description("API 描述")
                .license("http://springfox.github.io/springfox/docs/current/")
                .licenseUrl("http://springfox.github.io/springfox/docs/current/")
                .build();
    }
}