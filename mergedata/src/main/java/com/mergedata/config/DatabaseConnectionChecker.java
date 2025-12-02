package com.mergedata.config; // 或者其他合适的包

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    private final DruidDataSource dataSource;
    private final ApplicationContext context;

    public DatabaseConnectionChecker(DruidDataSource dataSource, ApplicationContext context) {
        this.dataSource = dataSource;
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=====================================================");
        System.out.println("🚀 [启动后同步检查] 正在强制获取数据库连接...");

        try (Connection conn = dataSource.getConnection()) {
            // 如果能获取连接，说明连接是健康的
            System.out.println("✅ [成功] 数据库连接验证通过，应用继续运行。");
        } catch (Exception e) {
            // 如果获取连接失败（即 IO 错误），则捕获异常并终止
            System.err.println("❌ [严重失败] 数据库连接初始化失败，应用将终止！");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误信息: " + e.getMessage());

            // ❗ 关键步骤：使用 SpringApplication.exit 强制终止应用
            // 返回码 1 表示非正常退出
            int exitCode = SpringApplication.exit(context, () -> 1);
            System.exit(exitCode);
        }
        System.out.println("=====================================================");
    }
}