package com.example.messagedataservice.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
public class ApiResponseBody {

    private List<Map<String, Object>> list;

    // Getters and Setters


}