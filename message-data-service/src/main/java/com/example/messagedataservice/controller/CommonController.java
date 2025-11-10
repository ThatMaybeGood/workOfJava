package com.example.messagedataservice.controller;

import org.springframework.web.bind.annotation.*;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:03
 */

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CommonController {

    @PostMapping("/data")String getData() {
        return "Common Data Response";
    }

    @GetMapping("/datas")String getDatas() {
        return "Common Data Response";
    }
}
