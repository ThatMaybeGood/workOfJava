package com.example.auto_demo.Controller;

import com.alibaba.fastjson.JSONObject;
import com.example.auto_demo.config.DynamicConfig;
import com.example.auto_demo.logic.ApiLogic;
import com.example.auto_demo.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DynamicConfig dynamicConfig;

    @Autowired
    private ApiLogic apiLogic;

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("token", dynamicConfig.getToken());
        result.put("session", dynamicConfig.getSession());
        result.put("lastCheckTime", lastCheckTime);
        result.put("healthStatus", lastHealthStatus);
        return result;
    }

    @PutMapping("/config")
    public Map<String, Object> saveConfig(@RequestBody Map<String, String> params) {
        String token = params.get("token");
        String session = params.get("session");

        if (token != null && !token.trim().isEmpty()) {
            dynamicConfig.setToken(token.trim());
        }
        if (session != null && !session.trim().isEmpty()) {
            dynamicConfig.setSession(session.trim());
        }

        Log.info("已更新 token 和 session");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置已保存");
        return result;
    }

    @PostMapping("/health")
    public Map<String, Object> healthCheck() {
        ApiLogic.HealthCheckResult check = apiLogic.healthCheck();
        String time = now();
        check.setCheckTime(time);
        lastCheckTime = time;
        lastHealthStatus = check.getStatus();

        Map<String, Object> result = new HashMap<>();
        result.put("valid", check.isValid());
        result.put("message", check.getMessage());
        result.put("status", check.getStatus());
        result.put("checkTime", check.getCheckTime());
        return result;
    }

    private String maskValue(String value) {
        if (value == null || value.length() <= 4) return "****";
        return value.substring(0, 3) + "***" + value.substring(value.length() - 3);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String lastCheckTime = "未检测";
    private static String lastHealthStatus = "未检测";
}
