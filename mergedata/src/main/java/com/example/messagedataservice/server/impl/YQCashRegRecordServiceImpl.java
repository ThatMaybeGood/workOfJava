package com.example.messagedataservice.server.impl;

import com.example.messagedataservice.dto.YQCashRegRecordDTO;
import com.example.messagedataservice.server.YQCashRegRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Slf4j
@Service
public class YQCashRegRecordServiceImpl implements YQCashRegRecordService {

    private static final String YQ_DATA_URL = "http://localhost:8080/api/yqData";
    private  RestTemplate restTemplate;

    @Override
    public List<YQCashRegRecordDTO> findByDate(LocalDate reportDate) {
        try {
            // 假设API返回的是YQCashRegRecord数组
            ResponseEntity<YQCashRegRecordDTO[]> response = restTemplate.getForEntity(YQ_DATA_URL, YQCashRegRecordDTO[].class, reportDate);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                log.error("Failed to get YQ data, status: {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("获取YQ数据异常", e);
            return new ArrayList<>();
        }    }
}
