package com.ch.cloud.conf;

import com.ch.Constants;
import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Optional;
import java.util.function.Function;


/**
 * desc: swagger2 Restful接口文档配置
 * 文档URL: http://localhost:7004/swagger2.html
 *
 * @author Zhimin Ma
 * @since 2018/10/26 10:42
 * @since 1.0
 */

//@EnableSwagger2
//@Configuration
// 启用swagger2功能注解
public class Swagger2Config {


    @Bean
    public Docket buildDocket() {
        //api文档实例

        //文档类型：DocumentationType.SWAGGER_2
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())    //.apiInfo(apiInfo())
                .select()
                .apis(basePackage("com.ch.cloud.kafka.controller" + Constants.SEPARATOR_2 + "com.ch.cloud.rocketmq.controller"))//要注释的接口名
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 声明基础包
     *
     * @param basePackage 基础包路径
     * @return
     */
    private static Predicate<RequestHandler> basePackage(final String basePackage) {
        return input -> declaringClass(input).map(handlerPackage(basePackage)).orElse(true);
    }

    /**
     * 校验基础包
     *
     * @param basePackage 基础包路径
     * @return
     */
    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage) {
        return input -> {
            for (String strPackage : basePackage.split(Constants.SEPARATOR_2)) {
                boolean isMatch = input.getPackage().getName().startsWith(strPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * 检验基础包实例
     *
     * @param requestHandler 请求处理类
     * @return
     */
    @SuppressWarnings("deprecation")
    private static Optional<Class<?>> declaringClass(RequestHandler requestHandler) {
        return Optional.ofNullable(requestHandler.declaringClass());
    }

    //构建 api文档的详细信息函数,注意这里的注解引用的是哪个
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("朝华 DevOps RESTFUL API")
                //创建人
                .contact(new Contact("CHAO HUA", "http://www.chaohua.com", "zhimin711@sina.com"))
                //版本号
                .version("1.0.0")
                //描述
                .description("API 描述")
                .license("http://springfox.github.io/springfox/docs/current/")
                .licenseUrl("http://springfox.github.io/springfox/docs/current/")
                .build();
    }
}