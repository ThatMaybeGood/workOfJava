package com.demo.integration.core.adapter;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:09
 */
public interface PostAdapter<REQ, RESP> {

    String bizCode();

    REQ buildRequest(Object bizData);

    RESP parseResponse(String response);
}
