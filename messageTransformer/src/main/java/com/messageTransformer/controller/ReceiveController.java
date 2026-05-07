package com.messageTransformer.controller;

import com.messageTransformer.service.MessageProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiveController {

    @Autowired
    private MessageProcessService processService;

    @PostMapping("/receive/order")
    public String order(@RequestBody String body) {
        return processService.process("/receive/order", body);
    }

    @PostMapping("/receive/user")
    public String user(@RequestBody String body) {
        return processService.process("/receive/user", body);
    }
}