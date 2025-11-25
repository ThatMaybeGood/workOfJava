package com.example.messagedataservice.server.impl;

import com.example.messagedataservice.model.HisData;
import com.example.messagedataservice.server.HisDataService;
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
public class HisDataServiceImpl implements HisDataService {

    private static final String HIS_DATA_URL = "http://localhost:8080/api/his-data";
    private RestTemplate restTemplate;
    @Override
    public List<HisData> findByDate(LocalDate reportDate) {
        try {
            // 假设API返回的是HisData数组
            ResponseEntity<HisData[]> response = restTemplate.getForEntity(HIS_DATA_URL, HisData[].class, reportDate);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                log.error("Failed to get HIS data, status: {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("获取HIS数据异常", e);
            return new ArrayList<>();
        }    }
}
