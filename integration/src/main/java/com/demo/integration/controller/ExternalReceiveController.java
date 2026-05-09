package com.demo.integration.controller;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 22:57
 */

 import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

 import javax.servlet.http.HttpServletRequest;
 import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/external")
public class ExternalReceiveController {

    @PostMapping("/receive")
    public ResponseEntity<String> receive(
            @RequestBody(required = false) String body,
            HttpServletRequest request) {

        log.info("========== 接收外部请求 ==========");

        log.info("uri={}", request.getRequestURI());

        log.info("headers={}", getHeaders(request));

        log.info("body={}", body);

        // 这里你自己处理业务
        // 可以转JSON
        // 可以解析Map
        // 可以落库

        String resp =
                "{\"code\":\"0\",\"msg\":\"success\"}";

        log.info("response={}", resp);

        return ResponseEntity.ok(resp);
    }

    private Map<String, String> getHeaders(
            HttpServletRequest request) {

        Map<String, String> map = new HashMap<>();

        Enumeration<String> names =
                request.getHeaderNames();

        while (names.hasMoreElements()) {

            String name = names.nextElement();

            map.put(name, request.getHeader(name));
        }

        return map;
    }
}
