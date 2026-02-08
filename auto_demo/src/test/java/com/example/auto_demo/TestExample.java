package com.example.auto_demo;

import com.example.auto_demo.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestExample {

    @Autowired
    private AppConfig appConfig;
    @Test
    void testMethod() {
        System.out.println("appConfig.toString() = " + appConfig.toString());

        AppConfig appConfig1 = new AppConfig();
        System.out.println("appConfig1.toString() = " + appConfig1.toString());

    }
}
