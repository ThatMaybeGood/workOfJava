package com.demo.integration.core.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:09
 */
public abstract class AbstractPostAdapter<REQ, RESP>
        implements PostAdapter<REQ, RESP> {

    protected ObjectMapper objectMapper =
            new ObjectMapper();

    protected abstract Class<RESP> responseClass();

    @Override
    public RESP parseResponse(String response) {

        try {

            return objectMapper.readValue(
                    response,
                    responseClass());

        } catch (Exception e) {

            throw new RuntimeException("响应解析失败", e);
        }
    }
}