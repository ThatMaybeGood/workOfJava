package com.mergedata.server.impl;

import com.mergedata.dto.YQOperatorDTO;
import com.mergedata.server.HisOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class HisOperatorServiceImpl implements HisOperatorService {
    private static final String OPERATOR_URL = "http://localhost:8080/api/operators";
    private RestTemplate restTemplate;
    @Override
    public List<YQOperatorDTO> findData() {
        try {
            // 假设API返回的是HisOperator数组
            ResponseEntity<YQOperatorDTO[]> response = restTemplate.getForEntity(OPERATOR_URL, YQOperatorDTO[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                log.error("Failed to get operators data, status: {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("获取操作员数据异常", e);
            return new ArrayList<>();
        }    }
}
