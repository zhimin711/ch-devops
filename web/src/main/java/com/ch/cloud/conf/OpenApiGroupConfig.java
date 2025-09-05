package com.ch.cloud.conf;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/3/7
 */
@Configuration
public class OpenApiGroupConfig {
    
    // 定义生产环境服务器
    Server prodServer = new Server()
            .url("https://114.132.54.246:7003")
            .description("生产环境");
    // 定义本地开发服务器
    Server devServer = new Server()
            .url("http://192.168.150.5:7004")
            .description("本地开发");
    
    // 添加OpenAPI全局配置
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CH Wiki API 文档")
                        .version("1.0.0")
                        .description("CH Wiki的API文档，包含所有模块的接口信息")).servers(Arrays.asList(devServer));
    }

    // 分组1：任务管理模块
    @Bean
    public GroupedOpenApi adminGroup() {
        return GroupedOpenApi.builder()
                .group("Nacos管理接口")       // 分组名称（显示在Swagger UI下拉框）
                .pathsToMatch("/nacos/**")  // 匹配路径规则
                .packagesToScan("com.ch.cloud.nacos.controller") // 指定扫描包
                .build();
    }

    // 分组1：任务管理模块
    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("开放接口")       // 分组名称（显示在Swagger UI下拉框）
                .pathsToMatch("/api/**")  // 匹配路径规则
                .packagesToScan("com.ch.cloud.wiki.plan.controller.api") // 指定扫描包
                .build();
    }

    // 分组1：任务管理模块
    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder()
                .group("授权接口")       // 分组名称（显示在Swagger UI下拉框）
                .pathsToMatch("/auth/**")  // 匹配路径规则
                .packagesToScan("com.ch.cloud.wiki.plan.controller.auth") // 指定扫描包

                // 新增参数配置
                .addOperationCustomizer(
                        (operation, handlerMethod) -> {
                            // 添加Header参数
                            operation.addParametersItem(
                                    new Parameter()
                                            .name("userId")
                                            .in(ParameterIn.HEADER.toString())
                                            .description("用户唯一标识")
                                            .required(true) // 根据需求设置是否必填
                                            .schema(new Schema<>().type("string"))
                            );
                            operation.addParametersItem(
                                    new Parameter()
                                            .name("appId")
                                            .in(ParameterIn.HEADER.toString())
                                            .description("应用唯一标识")
                                            .required(true) // 根据需求设置是否必填
                                            .schema(new Schema<>().type("string"))
                            );
//                            operation.addParametersItem(
//                                    new Parameter()
//                                            .name("X-API-KEY")
//                                            .in(ParameterIn.HEADER.toString())
//                                            .required(false) // 根据需求设置是否必填
//                                            .schema(new Schema<>().type("string"))
//                            );
                            SecurityRequirement securityRequirement = new SecurityRequirement();
                            securityRequirement.addList("X-API-KEY");
                            operation.addSecurityItem(securityRequirement);
                            return operation;
                        }
                )
                .build();
    }

    // 默认分组（显示所有API）
    @Bean
    public GroupedOpenApi defaultGroup() {
        return GroupedOpenApi.builder()
                .group("全部接口")
                .pathsToMatch("/**")
                .build();
    }
}