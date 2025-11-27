package com.mergedata.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponseBody<T> {

    private List<T> list;

    // Getters and Setters


}