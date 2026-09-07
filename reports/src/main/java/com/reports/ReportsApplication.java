package com.reports;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 *
 * Reports Gateway Application
 *
 * @author Reports Team
 */
@SpringBootApplication
public class ReportsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportsApplication.class, args);
    }

    /**
     * 启动完成后打印首页访问地址
     */
    @Slf4j
    @Component
    static class HomePagePrinter implements ApplicationRunner {

        private final Environment environment;

        HomePagePrinter(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void run(ApplicationArguments args) {
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            if (contextPath.endsWith("/")) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }
            String host = environment.getProperty("server.address", "localhost");
            log.info("============================================================");
            log.info("首页地址: http://{}:{}{}/index.html", host, port, contextPath);
            log.info("============================================================");
        }
    }

}
