package com.etl.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ETL通用数据抽取平台API")
                        .version("1.0.0")
                        .description("ETL通用数据抽取平台 - 支持存储过程/SQL/视图/表/HTTP/文件等多种数据源抽取")
                        .contact(new Contact()
                                .name("ETL Team")
                                .email("etl@example.com")));
    }
}
