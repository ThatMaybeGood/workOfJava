package com.showexcel.service.impl;

import com.showexcel.model.CashStatistics;
import com.showexcel.repository.CashStatisticsRepository;
import com.showexcel.response.CashStatisticsResponse;
import com.showexcel.response.RowData;
import com.showexcel.response.TableRow;
import com.showexcel.service.CashStatisticsNewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class CashStatisticsServiceNewImpl implements CashStatisticsNewService {

    @Autowired
    private CashStatisticsRepository cashStatisticsRepository;

    //获取当前日期时间，例如2025-10-29并将其格式化为字符串形式
    LocalDateTime now = LocalDateTime.now().minusDays(1);
    String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    @Override
    public CashStatisticsResponse getAllStatisticsTableByDate(String date) {

        // 一次性查询所有数据
        List<RowData> allData = cashStatisticsRepository.findByTableDateNew(date);

        return null;
    }
}
