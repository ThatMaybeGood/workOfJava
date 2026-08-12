package com.etl;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
@MapperScan("com.etl.mapper")
public class EtlApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtlApplication.class, args);
        log.info("=== ETL通用数据抽取平台启动成功 ===");
        log.info("首页: http://localhost:18880/index.html");
    }
}
