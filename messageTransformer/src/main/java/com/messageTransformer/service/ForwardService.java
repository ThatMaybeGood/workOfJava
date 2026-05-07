package com.messageTransformer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ForwardService {

    @Autowired
    private RestTemplate restTemplate;

    public String post(String url, Object body) {
        return restTemplate.postForObject(url, body, String.class);
    }
}