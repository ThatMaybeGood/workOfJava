package com.example.messagedataservice.server.impl;

import com.example.messagedataservice.dto.HolidayCalendarDTO;
import com.example.messagedataservice.server.HolidayCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class HolidayCalendarServiceImpl implements HolidayCalendarService {
    private static final String HIS_DATA_URL = "http://localhost:8080/api/holiday-calendar";

    private  RestTemplate restTemplate;

    @Override
    public List<HolidayCalendarDTO> findByDate(LocalDate date) {
            try {
                // 假设API返回的是HisData数组
                ResponseEntity<HolidayCalendarDTO[]> response = restTemplate.getForEntity(HIS_DATA_URL, HolidayCalendarDTO[].class);
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    return Arrays.asList(response.getBody());
                } else {
                    log.error("Failed to get holiday calendar, status: {}", response.getStatusCode());
                    return new ArrayList<>();
                }
            } catch (Exception e) {
                log.error("获取节假日日历异常", e);
                return new ArrayList<>();
            }
        }
}
