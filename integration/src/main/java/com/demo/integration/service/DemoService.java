package com.demo.integration.service;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:07
 */

import com.demo.integration.core.executor.PostExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    @Autowired
    private PostExecutor postExecutor;

    public void test() {

        Object resp = postExecutor.execute("spd", null);

        System.out.println(resp);
    }
}