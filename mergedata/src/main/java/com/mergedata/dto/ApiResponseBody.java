package com.mergedata.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ApiResponseBody<T> {

    private T list;

    // Getters and Setters


}