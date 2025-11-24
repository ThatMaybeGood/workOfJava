package com.example.messagedataservice.server;

import com.alibaba.druid.pool.DruidDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DruidCheckService {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void checkDruid() {
        if (dataSource instanceof DruidDataSource) {
            System.out.println("✅ Druid 数据源配置成功");
            DruidDataSource druid = (DruidDataSource) dataSource;
            System.out.println("初始连接数: " + druid.getInitialSize());
            System.out.println("最大连接数: " + druid.getMaxActive());
        } else {
            System.out.println("❌ Druid 数据源配置失败，使用的是: " + dataSource.getClass().getName());
        }
    }
}