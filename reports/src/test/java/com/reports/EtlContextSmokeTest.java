package com.reports;

import com.reports.aspect.EtlTriggerAspect;
import com.reports.service.EtlClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 容器装配冒烟：验证真实 Spring 组件扫描下 EtlClient / EtlTriggerAspect 能被自动装配。
 *
 * <p>默认 @Disabled：会启动完整 Spring Boot 上下文（较慢），常规全量测试不跑。
 * 需要验证真实容器自动装配时，临时去掉 @Disabled 后执行：
 * {@code mvn test -Dtest=EtlContextSmokeTest}（不依赖数据库，连接池懒初始化不会连库）。</p>
 */
@Disabled("真实容器冒烟较慢，默认关闭；需要验证装配时临时放开")
@SpringBootTest
class EtlContextSmokeTest {

    @Resource
    private EtlClient etlClient;

    @Resource
    private EtlTriggerAspect etlTriggerAspect;

    @Test
    void context_loads_and_wires_etl_beans() {
        assertNotNull(etlClient, "EtlClient 应能被组件扫描找到并注入");
        assertNotNull(etlTriggerAspect, "EtlTriggerAspect 应能被自动装配 EtlClient");
    }
}
