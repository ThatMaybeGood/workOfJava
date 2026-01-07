package com.example.auto_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AutoDemoApplication {

    public static void main(String[] args) {
        // 1. 运行并获取上下文对象 context
        ConfigurableApplicationContext context = SpringApplication.run(AutoDemoApplication.class, args);

        // 2. 打印诊断信息
        String webType = context.getEnvironment().getProperty("spring.main.web-application-type");
        System.out.println("======= 诊断信息 =======");
        System.out.println("当前 Web 类型: " + webType);
        System.out.println("Tomcat 是否存在: " + context.containsBean("tomcatServletWebServerFactory"));
        System.out.println("=======================");
    }
}