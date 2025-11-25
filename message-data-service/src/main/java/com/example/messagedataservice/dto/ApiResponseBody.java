package com.example.messagedataservice.dto;

import java.util.List;
import java.util.Map;

public class ApiResponseBody {
    private Integer totalCount;

    // 使用 List<Map<...>> 来接收包含不同结构的 List 元素
    private List<Map<String, Object>> list;

    // Getters and Setters


    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<Map<String, Object>> getList() {
        return list;
    }

    public void setList(List<Map<String, Object>> list) {
        this.list = list;
    }
}