package com.mergedata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.lang.reflect.Method;

@MapperScan("com.mergedata.mapper") // Mapper 接口的实际包路径
@EnableCaching // 开启缓存功能
@SpringBootApplication
public class MessageDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageDataApplication.class, args);
        System.out.println("swaggerUrl地址：http://localhost:18081/swagger-ui/index.html");
    }
}
