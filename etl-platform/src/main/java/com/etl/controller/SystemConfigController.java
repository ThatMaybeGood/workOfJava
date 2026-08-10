package com.etl.controller;

import com.etl.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统设置 —— 通过可视化界面修改端口、元数据库、日志路径等运行参数。
 * 保存写入 jar 同目录 ./config/application.yml（由 application.yml 的
 * spring.config.additional-location 自动加载，优先级高于 jar 内配置），重启服务后生效。
 */
@Slf4j
@RestController
@RequestMapping("/api/etl/settings")
@Tag(name = "系统设置", description = "运行时参数的可视化配置（端口/元数据库/日志）")
public class SystemConfigController {

    private static final String CONFIG_DIR = "./config";
    private static final String CONFIG_FILE = CONFIG_DIR + "/application.yml";

    @Autowired
    private Environment environment;

    @GetMapping
    @Operation(summary = "获取当前生效的系统设置")
    public ApiResponse<Map<String, Object>> getSettings() {
        Map<String, Object> result = new LinkedHashMap<>();

        String port = environment.getProperty("server.port", "18880");
        result.put("port", port);

        String dbUrl = environment.getProperty("spring.datasource.url", "");
        boolean isH2 = dbUrl != null && dbUrl.contains("jdbc:h2:");
        result.put("dbMode", isH2 ? "h2" : "external");

        if (isH2) {
            // H2：展示文件库路径（若仍是内存库则给默认文件库建议）
            String h2Path = extractH2Path(dbUrl);
            result.put("h2DbPath", h2Path != null ? h2Path : "./data/etl_platform");
        } else {
            result.put("dbUrl", dbUrl);
            result.put("dbUsername", environment.getProperty("spring.datasource.username", ""));
        }

        result.put("logPath", environment.getProperty("logging.file.name", "logs/etl-platform-prod.log"));
        return ApiResponse.success(result);
    }

    @PutMapping
    @Operation(summary = "保存系统设置（写入外部配置文件，重启后生效）")
    public ApiResponse<Map<String, Object>> saveSettings(@RequestBody Map<String, Object> body) {
        Map<String, Object> config = new LinkedHashMap<>();

        // 端口校验
        String portStr = String.valueOf(body.getOrDefault("port", ""));
        int port;
        try {
            port = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException e) {
            return ApiResponse.error("端口必须是数字");
        }
        if (port < 1 || port > 65535) {
            return ApiResponse.error("端口必须在 1-65535 之间");
        }
        config.put("server", nested("port", port));

        // 元数据库
        String dbMode = String.valueOf(body.getOrDefault("dbMode", "h2"));
        if ("external".equals(dbMode)) {
            String url = str(body.get("dbUrl"));
            String username = str(body.get("dbUsername"));
            String password = str(body.get("dbPassword"));
            if (url.isEmpty()) return ApiResponse.error("外部数据库连接URL不能为空");
            Map<String, Object> ds = new LinkedHashMap<>();
            ds.put("url", url);
            if (!username.isEmpty()) ds.put("username", username);
            if (!password.isEmpty()) ds.put("password", password);
            config.put("spring", nested("datasource", ds));
        } else {
            // H2 文件库：持久化，重启不丢数据
            String h2Path = str(body.get("h2DbPath"));
            if (h2Path.isEmpty()) h2Path = "./data/etl_platform";
            // 文件库持久化；首次连接时自动执行建表脚本（h2_init.sql 为 CREATE TABLE IF NOT EXISTS，幂等）
            String h2Url = "jdbc:h2:file:" + h2Path + ";MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:db/h2_init.sql'";
            Map<String, Object> datasource = new LinkedHashMap<>();
            datasource.put("type", "com.alibaba.druid.pool.DruidDataSource");
            datasource.put("driver-class-name", "org.h2.Driver");
            datasource.put("url", h2Url);
            datasource.put("username", "sa");
            datasource.put("password", "");
            config.put("spring", nested("datasource", datasource));
        }

        // 日志路径
        String logPath = str(body.get("logPath"));
        if (!logPath.isEmpty()) {
            Map<String, Object> logging = new LinkedHashMap<>();
            logging.put("file", nested("name", logPath));
            config.put("logging", logging);
        }

        // 写外部配置文件
        try {
            File dir = new File(CONFIG_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                return ApiResponse.error("无法创建配置目录: " + dir.getAbsolutePath());
            }
            File file = new File(CONFIG_FILE);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);
//            try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
//                yaml.dump(config, fw);
//            }
            try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                yaml.dump(config, writer);
            }
            log.info("系统设置已保存到 {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("保存系统设置失败", e);
            return ApiResponse.error("保存失败: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("restartRequired", true);
        result.put("configFile", CONFIG_FILE);
        return ApiResponse.success(result, "设置已保存，重启服务后生效");
    }

    private static Map<String, Object> nested(String key, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(key, value);
        return m;
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String extractH2Path(String url) {
        if (url == null) return null;
        String prefix = "jdbc:h2:file:";
        if (url.startsWith(prefix)) {
            String rest = url.substring(prefix.length());
            int idx = rest.indexOf(';');
            return idx >= 0 ? rest.substring(0, idx) : rest;
        }
        return null;
    }
}
